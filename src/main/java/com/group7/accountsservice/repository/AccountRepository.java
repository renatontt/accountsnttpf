package com.group7.accountsservice.repository;

import com.group7.accountsservice.model.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface AccountRepository extends ReactiveMongoRepository<Account,String> {

    Flux<Account> findAccountByClientAndType(String client, String type);

}
