package com.group7.accountsservice.repository;


import com.group7.accountsservice.model.Transfer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface TransferRepository extends ReactiveMongoRepository<Transfer,String> {
    Flux<Transfer> findByFromOrTo(String from, String to);
    Flux<Transfer> findByFromOrToAndDateBetween(String accountFrom,String accountTo, LocalDate from, LocalDate to);
}
