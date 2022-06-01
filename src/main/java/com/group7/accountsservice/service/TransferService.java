package com.group7.accountsservice.service;

import com.group7.accountsservice.dto.TransferRequest;
import com.group7.accountsservice.dto.TransferResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransferService {

    Flux<TransferResponse> getAll();

    Flux<TransferResponse> getAllByAccount(String account);

    Mono<TransferResponse> getById(String id);

    Mono<Void> delete(String id);

    Mono<Void> deleteAll();

    Mono<TransferResponse> save(TransferRequest transferRequest);

    Mono<TransferResponse> payTransaction(TransferRequest transferRequest);

    Mono<TransferResponse> update(String id,TransferRequest transferRequest);
}
