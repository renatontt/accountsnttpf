package com.group7.accountsservice.repository;

import com.group7.accountsservice.model.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard,String> {
    Flux<DebitCard> findCardByClient(String client);
    Flux<DebitCard> findCardByNumber(String number);

    Flux<DebitCard> findCardByMainAccount(String number);
}
