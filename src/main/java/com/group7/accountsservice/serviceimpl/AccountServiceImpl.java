package com.group7.accountsservice.serviceimpl;

import com.group7.accountsservice.dto.*;
import com.group7.accountsservice.exception.account.AccountCreationException;
import com.group7.accountsservice.exception.account.AccountNotFoundException;
import com.group7.accountsservice.model.Account;
import com.group7.accountsservice.model.DebitCard;
import com.group7.accountsservice.model.Movement;
import com.group7.accountsservice.repository.AccountRepository;
import com.group7.accountsservice.repository.DebitCardRepository;
import com.group7.accountsservice.repository.MovementRepository;
import com.group7.accountsservice.repository.TransferRepository;
import com.group7.accountsservice.service.AccountService;
import com.group7.accountsservice.utils.AccountUtils;
import com.group7.accountsservice.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private static final String NOT_FOUND_MESSAGE = "Account not found with id: ";
    private static final String NOT_FOUND_MESSAGE_WITH_ID = "Account not found with id: {}";

    private AccountRepository accountRepository;
    private AccountUtils accountUtils;
    private WebClientUtils webClientUtils;

    private MovementRepository movementRepository;

    private DebitCardRepository debitCardRepository;

    private TransferRepository transferRepository;

    @Override
    public Flux<AccountResponse> getAll() {
        return accountRepository.findAll()
                .map(AccountResponse::fromModel)
                .doOnComplete(() -> log.info("Retrieving all Accounts"));
    }

    @Override
    public Flux<AccountResponse> getAllByClient(String client) {
        return accountRepository.findAccountByClient(client)
                .map(AccountResponse::fromModel)
                .doOnComplete(() -> log.info("Retrieving all Accounts"));
    }

    @Override
    public Mono<AccountReportResponse> getReport(String id, LocalDate from, LocalDate to) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(NOT_FOUND_MESSAGE + id)))
                .doOnError(ex -> log.error(NOT_FOUND_MESSAGE_WITH_ID, id, ex))
                .flatMap(account -> {
                    AccountReportResponse report = AccountReportResponse.fromModel(account);

                    Mono<DebitCardResponse> debitCard = debitCardRepository
                            .findCardByMainAccount(id)
                            .defaultIfEmpty(new DebitCard())
                            .next()
                            .map(DebitCardResponse::fromModel);

                    Mono<List<MovementResponse>> movements = movementRepository
                            .findByAccountAndDateBetween(id, from, to)
                            .map(MovementResponse::fromModel)
                            .collectList();

                    Mono<List<FeeResponse>> fees = movementRepository.findByAccountAndDateBetween(id, from, to)
                            .filter(movement -> movement.getTransactionFee() > 0.0)
                            .map(movement -> new FeeResponse(movement.getDate(), movement.getTransactionFee()))
                            .collectList();

                    Mono<List<TransferResponse>> transfers = transferRepository.findByFromOrToAndDateBetween(id,id,from,to)
                            .map(TransferResponse::fromModel)
                            .collectList();

                    return Mono.zip(debitCard,movements,fees,transfers)
                            .map(result -> {
                                report.setDebitCard(result.getT1());
                                report.setMovements(result.getT2());
                                report.setFees(result.getT3());
                                report.setTransfers(result.getT4());
                                return report;
                            })
                            .doOnError(err -> log.error("Error",err));
                });
    }

    @Override
    public Mono<AccountResponse> getById(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(NOT_FOUND_MESSAGE + id)))
                .doOnError(ex -> log.error(NOT_FOUND_MESSAGE_WITH_ID, id, ex))
                .map(AccountResponse::fromModel);
    }

    @Override
    public Mono<Void> delete(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(NOT_FOUND_MESSAGE + id)))
                .doOnError(ex -> log.error(NOT_FOUND_MESSAGE_WITH_ID, id, ex))
                .flatMap(existingAccount ->
                        accountRepository.delete(existingAccount)
                )
                .doOnSuccess(ex -> log.info("Delete account with id: {}", id));
    }

    @Override
    public Mono<Void> deleteAll() {
        return accountRepository.deleteAll()
                .doOnSuccess(ex -> log.info("Delete all accounts"));
    }

    @Override
    public Mono<AccountResponse> save(AccountRequest accountRequest) {
        return Mono.just(accountRequest)
                .flatMap(account -> webClientUtils.getClient(account.getClient())
                        .flatMap(accountClient -> {
                            account.setClientType(accountClient.getType());
                            account.setClientProfile(accountClient.getProfile());

                            if (accountClient.getProfile().equalsIgnoreCase("VIP") || accountClient.getProfile().equalsIgnoreCase("PYME")) {
                                return webClientUtils.getCredits(accountClient.getId())
                                        .hasElements()
                                        .flatMap(hasElements -> {
                                            if (!hasElements){
                                                return Mono.error(new AccountCreationException(accountClient.getProfile().toUpperCase()+ " Client must have a credit cart"));
                                            }
                                            return Mono.just(account);
                                        });
                            }

                            if (accountClient.getType().equalsIgnoreCase("Personal")) {
                                return accountRepository.findAccountByClientAndType(accountClient.getId(), account.getType())
                                        .hasElements()
                                        .flatMap(hasElements -> {
                                            if (hasElements)
                                                return Mono.error(new AccountCreationException("Client already have a " + account.getType() + " account"));
                                            return Mono.just(account);
                                        });
                            }

                            return Mono.just(account);
                        }))
                .map(AccountRequest::toModel)
                .flatMap(account -> {
                    accountUtils.setMaintenanceFee(account);
                    accountUtils.setMovementsLimit(account);
                    return accountRepository.save(account);
                })
                .map(AccountResponse::fromModel)
                .onErrorMap(ex -> new AccountCreationException(ex.getMessage()))
                .doOnSuccess(res -> log.info("Created new account with ID: {}", res.getId()))
                .doOnError(ex -> log.error("Error creating new Account ", ex));
    }

    @Override
    public Mono<AccountResponse> update(String id, AccountRequest accountRequest) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(NOT_FOUND_MESSAGE + id)))
                .doOnError(ex -> log.error(NOT_FOUND_MESSAGE_WITH_ID, id, ex))
                .flatMap(existingAccount -> {
                    if (existingAccount.getType().equals("Fixed Deposit"))
                        existingAccount.setMovementDay(accountRequest.getMovementDay());
                    return accountRepository.save(existingAccount);
                })
                .map(AccountResponse::fromModel)
                .doOnSuccess(res -> log.info("Updated account with ID: {}", res.getId()));
    }
}
