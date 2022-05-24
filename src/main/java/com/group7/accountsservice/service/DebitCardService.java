package com.group7.accountsservice.service;

import com.group7.accountsservice.dto.DebitCardRequest;
import com.group7.accountsservice.dto.DebitCardResponse;
import com.group7.accountsservice.dto.MovementRequest;
import com.group7.accountsservice.dto.MovementResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DebitCardService {

    Flux<DebitCardResponse> getAll();

    Flux<DebitCardResponse> getAllByClient(String client);

    Mono<DebitCardResponse> getById(String id);

    Mono<Void> delete(String id);

    Mono<Void> deleteAll();

    Mono<DebitCardResponse> save(DebitCardRequest debitCardRequest);

    Mono<List<MovementResponse>> makeMovement(MovementRequest movementRequest);

    Flux<MovementResponse> getLastMovements(String id);

    Mono<Double> getBalanceOfMainAccount(String id);

    Mono<DebitCardResponse> link(DebitCardRequest debitCardRequest);

    Mono<DebitCardResponse> update(String id,DebitCardRequest debitCardRequest);
}
