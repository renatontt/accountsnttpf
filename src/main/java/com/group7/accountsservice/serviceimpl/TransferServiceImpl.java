package com.group7.accountsservice.serviceimpl;

import com.group7.accountsservice.dto.*;
import com.group7.accountsservice.exception.transfer.TransferCreationException;
import com.group7.accountsservice.exception.transfer.TransferNotFoundException;
import com.group7.accountsservice.model.Account;
import com.group7.accountsservice.model.Movement;
import com.group7.accountsservice.model.Transfer;
import com.group7.accountsservice.repository.AccountRepository;
import com.group7.accountsservice.repository.MovementRepository;
import com.group7.accountsservice.repository.TransferRepository;
import com.group7.accountsservice.service.TransferService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

@Service
@Slf4j
public class TransferServiceImpl implements TransferService {

    private static final String NOT_FOUND_MESSAGE = "Movement not found with id: ";

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private MessageService messageService;

    private RMapReactive<String, Transaction> transactionMap;

    public TransferServiceImpl(RedissonReactiveClient client) {
        this.transactionMap = client.getMap("transaction", new TypedJsonJacksonCodec(String.class, Transaction.class));
    }


    @Override
    public Flux<TransferResponse> getAll() {
        return transferRepository.findAll()
                .map(TransferResponse::fromModel);
    }

    @Override
    public Flux<TransferResponse> getAllByAccount(String account) {
        return transferRepository.findByFromOrTo(account, account)
                .map(TransferResponse::fromModel);
    }

    @Override
    public Mono<TransferResponse> getById(String id) {
        return transferRepository.findById(id)
                .switchIfEmpty(Mono.error(new TransferNotFoundException(NOT_FOUND_MESSAGE + id)))
                .map(TransferResponse::fromModel);
    }


    @Override
    public Mono<Void> delete(String id) {
        return transferRepository.findById(id)
                .switchIfEmpty(Mono.error(new TransferNotFoundException(NOT_FOUND_MESSAGE + id)))
                .flatMap(existingAccount ->
                        transferRepository.delete(existingAccount)
                );
    }

    @Override
    public Mono<Void> deleteAll() {
        return transferRepository.deleteAll();
    }


    @Override
    public Mono<TransferResponse> save(TransferRequest transferRequest) {

        Mono<Account> accountFrom = accountRepository.findById(transferRequest.getFrom());
        Mono<Account> accountTo = accountRepository.findById(transferRequest.getTo());

        return accountFrom
                .zipWith(accountTo)
                .flatMap(accounts -> {
                    Account from = accounts.getT1();
                    Account to = accounts.getT2();

                    if (from.getBalance() < transferRequest.getAmount())
                        return Mono.error(new TransferCreationException("Not enough money to make this transfer"));

                    if (from.getClient().equals(to.getClient()))
                        return transferSameClient(transferRequest, from, to);

                    return transferSameClient(transferRequest, from, to);
                }).map(TransferResponse::fromModel);
    }

    @Override
    public Mono<TransferResponse> payTransaction(TransferRequest transferRequest) {

        Mono<Account> accountFrom = accountRepository.findById(transferRequest.getFrom())
                .switchIfEmpty(Mono.error(new TransferCreationException("Account does not exist")));
        Mono<Transaction> transactionMono = transactionMap.get(transferRequest.getTransaction())
                .switchIfEmpty(Mono.error(new TransferCreationException("There is not transaction with this ID")));

        return accountFrom
                .zipWith(transactionMono)
                .flatMap(accounts -> {
                    Account from = accounts.getT1();
                    Transaction transaction = accounts.getT2();

                    if (transaction.getState().equalsIgnoreCase("Expired") ||
                            transaction.getExpiration().isBefore(LocalDateTime.now())
                    ) {
                        return Mono.error(new TransferCreationException("The transaction request has expired"));
                    }

                    if (!Objects.equals(transaction.getAmountFx(), transferRequest.getAmount()))
                        return Mono.error(new TransferCreationException("The transaction is for this amount:" + transferRequest.getAmount()));

                    log.info("Transaction number: {}",transaction.getNumber());
                    log.info("Transfer number: {}",transferRequest.getFrom());

                    if (!Objects.equals(transaction.getNumber(), transferRequest.getFrom()))
                        return Mono.error(new TransferCreationException("Incorrect account for source transaction"));

                    messageService.sendTransaction(TransactionEvent.builder()
                            .transactionId(transferRequest.getTransaction())
                            .state("Paid")
                            .amount(transferRequest.getAmount())
                            .build());

                    transferRequest.setTo("");

                    return transferTransaction(transferRequest, from);
                }).map(TransferResponse::fromModel);
    }

    public Mono<Transfer> transferSameClient(TransferRequest transferRequest, Account from, Account to) {

        Mono<Movement> newMovementOut = movementRepository.save(new Movement(null,
                "Transfer Out",
                transferRequest.getAmount(),
                0.0,
                LocalDate.now(),
                from.getId()));

        Mono<Movement> newMovementIn = movementRepository.save(new Movement(null,
                "Transfer In",
                transferRequest.getAmount(),
                0.0,
                LocalDate.now(),
                to.getId()));

        from.setBalance(from.getBalance() - transferRequest.getAmount());
        to.setBalance(to.getBalance() + transferRequest.getAmount());

        Mono<Account> updatedFromAccount = accountRepository.save(from);
        Mono<Account> updatedToAccount = accountRepository.save(to);

        return Mono.zip(newMovementOut, newMovementIn, updatedFromAccount, updatedToAccount)
                .then(Mono.just(transferRequest))
                .map(TransferRequest::toModel)
                .flatMap(transfer -> transferRepository.save(transfer));
    }

    public Mono<Transfer> transferTransaction(TransferRequest transferRequest, Account from) {

        Mono<Movement> newMovementOut = movementRepository.save(new Movement(null,
                "Pay Transaction",
                transferRequest.getAmount(),
                0.0,
                LocalDate.now(),
                from.getId()));

        from.setBalance(from.getBalance() - transferRequest.getAmount());

        Mono<Account> updatedFromAccount = accountRepository.save(from);

        return Mono.zip(newMovementOut, updatedFromAccount)
                .then(Mono.just(transferRequest))
                .map(TransferRequest::toModel)
                .flatMap(transfer -> transferRepository.save(transfer));
    }

    @Override
    public Mono<TransferResponse> update(String id, TransferRequest transferRequest) {

        return Mono.just(transferRequest)
                .map(TransferRequest::toModel)
                .flatMap(transfer -> transferRepository.save(transfer))
                .map(TransferResponse::fromModel)
                .onErrorMap(ex -> new TransferNotFoundException(ex.getMessage()));

    }


    @Bean
    Consumer<TransactionEvent> transaction() {
        return transactionEvent -> {
            if (transactionEvent.getState().equals("Transfer")) {
                accountRepository.findById(transactionEvent.getNumber())
                        .flatMap(account -> movementRepository.save(Movement.builder()
                                        .type("Receive Transaction")
                                        .amount(transactionEvent.getAmount())
                                        .transactionFee(0.0)
                                        .date(LocalDate.now())
                                        .account(account.getId())
                                        .build())
                                .thenReturn(account))
                        .flatMap(accountAux -> {
                            accountAux.setBalance(accountAux.getBalance() + transactionEvent.getAmount());
                            transactionEvent.setState("Completed");
                            messageService.sendTransaction(transactionEvent);
                            return accountRepository.save(accountAux);
                        })
                        .subscribe();
            }
        };
    }

}
