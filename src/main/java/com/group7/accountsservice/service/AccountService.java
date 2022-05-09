package com.group7.accountsservice.service;

import com.group7.accountsservice.dto.AccountRequest;
import com.group7.accountsservice.dto.AccountResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Flux<AccountResponse> getAll();

    Flux<AccountResponse> getAllByClient(String client);

    Mono<AccountResponse> getById(String id);

    Mono<Void> delete(String id);

    Mono<Void> deleteAll();

    Mono<AccountResponse> save(AccountRequest accountRequest);

    Mono<AccountResponse> update(String id,AccountRequest accountRequest);
}
