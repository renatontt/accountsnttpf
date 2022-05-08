package com.group7.accountsservice.serviceImpl;

import com.group7.accountsservice.dto.AccountRequest;
import com.group7.accountsservice.dto.AccountResponse;
import com.group7.accountsservice.exception.account.AccountCreationException;
import com.group7.accountsservice.exception.account.AccountNotFoundException;
import com.group7.accountsservice.repository.AccountRepository;
import com.group7.accountsservice.service.AccountService;
import com.group7.accountsservice.utils.AccountUtils;
import com.group7.accountsservice.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private AccountRepository accountRepository;
    private AccountUtils accountUtils;
    private WebClientUtils webClientUtils;

    @Override
    public Flux<AccountResponse> getAll() {
        return accountRepository.findAll()
                .map(AccountResponse::fromModel);
    }

    @Override
    public Mono<AccountResponse> getById(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with id: " + id)))
                .map(AccountResponse::fromModel);
    }

    @Override
    public Mono<Void> delete(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with id: " + id)))
                .flatMap(existingAccount ->
                        accountRepository.delete(existingAccount)
                );
    }

    @Override
    public Mono<Void> deleteAll() {
        return accountRepository.deleteAll();
    }

    @Override
    public Mono<AccountResponse> save(AccountRequest accountRequest) {
        return Mono.just(accountRequest)
                .flatMap(account -> webClientUtils.getClient(account.getClient())
                        .flatMap(accountClient -> {
                            account.setClientType(accountClient.getType());
                            if (accountClient.getType().equals("Personal")) {
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
                .onErrorMap(ex -> new AccountCreationException(ex.getMessage()));
    }

    @Override
    public Mono<AccountResponse> update(String id, AccountRequest accountRequest) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with id: " + id)))
                .flatMap(existingAccount -> {
                    if (existingAccount.getType().equals("Fixed Deposit"))
                        existingAccount.setMovementDay(accountRequest.getMovementDay());
                    return accountRepository.save(existingAccount);
                })
                .map(AccountResponse::fromModel);
    }
}
