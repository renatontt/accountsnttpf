package com.group7.accountsservice.service;

import com.group7.accountsservice.dto.MovementRequest;
import com.group7.accountsservice.dto.MovementResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementService {

    Flux<MovementResponse> getAll();

    Mono<MovementResponse> getById(String id);

    Flux<MovementResponse> getAllMovementsByAccount(String account);

    Mono<Void> delete(String id);

    Mono<Void> deleteAll();

    Mono<MovementResponse> save(MovementRequest movementRequest);

    Mono<MovementResponse> update(String id,MovementRequest movementRequest);

}
