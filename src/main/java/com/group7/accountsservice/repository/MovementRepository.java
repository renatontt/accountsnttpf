package com.group7.accountsservice.repository;

import com.group7.accountsservice.model.Movement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import java.util.Date;

public interface MovementRepository extends ReactiveMongoRepository<Movement,String> {
    Flux<Movement> findByAccountAndDateBetween(String account, Date from, Date to);
    Flux<Movement> findByAccount(String account);

}
