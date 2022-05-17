package com.group7.accountsservice.repository;

import com.group7.accountsservice.model.Transfer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransferRepository extends ReactiveMongoRepository<Transfer,String> {
    Flux<Transfer> findByFromOrTo(String from, String to);

}
