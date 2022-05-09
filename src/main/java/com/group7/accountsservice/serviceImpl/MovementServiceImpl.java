package com.group7.accountsservice.serviceImpl;

import com.group7.accountsservice.dto.MovementRequest;
import com.group7.accountsservice.dto.MovementResponse;
import com.group7.accountsservice.exception.movement.MovementCreationException;
import com.group7.accountsservice.model.Account;
import com.group7.accountsservice.model.Movement;
import com.group7.accountsservice.repository.AccountRepository;
import com.group7.accountsservice.repository.MovementRepository;
import com.group7.accountsservice.service.MovementService;
import com.group7.accountsservice.utils.DateUtils;
import com.group7.accountsservice.utils.MovementUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@AllArgsConstructor
public class MovementServiceImpl implements MovementService {

    private MovementRepository movementRepository;

    private AccountRepository accountRepository;

    @Override
    public Flux<MovementResponse> getAll() {
        return movementRepository.findAll()
                .map(MovementResponse::fromModel);
    }

    @Override
    public Mono<MovementResponse> getById(String id) {
        return movementRepository.findById(id)
                .switchIfEmpty(Mono.error(new MovementCreationException("Movement not found with id: " + id)))
                .map(MovementResponse::fromModel);
    }

    @Override
    public Flux<MovementResponse> getAllMovementsByAccount(String account) {
        return movementRepository.findByAccount(account)
                .map(MovementResponse::fromModel);
    }

    @Override
    public Mono<Void> delete(String id) {
        return movementRepository.findById(id)
                .switchIfEmpty(Mono.error(new MovementCreationException("Movement not found with id: " + id)))
                .flatMap(existingAccount ->
                        movementRepository.delete(existingAccount)
                );
    }

    @Override
    public Mono<Void> deleteAll() {
        return movementRepository.deleteAll();
    }

    private Mono<Long> getMovementsOfCurrentMonthByAccount(String account) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate firstOfMonth = currentMonth.atDay(1);
        LocalDate last = currentMonth.atEndOfMonth();
        return movementRepository.findByAccountAndDateBetween(account, DateUtils.asDate(firstOfMonth), DateUtils.asDate(last))
                .count();
    }

    @Override
    public Mono<MovementResponse> save(MovementRequest movementRequest) {
        return Mono.just(movementRequest)
                .map(MovementRequest::toModel)
                .flatMap(movement -> accountRepository.findById(movement.getAccount())
                        .switchIfEmpty(Mono.error(new MovementCreationException("Account not found with id: " + movement.getAccount())))
                        .zipWith(getMovementsOfCurrentMonthByAccount(movement.getAccount()))
                        .flatMap(result -> {
                            Account existingAccount = result.getT1();
                            Long movementsCurrentMonth = result.getT2();
                            if (!existingAccount.isMovementValid(movement.getType(), movement.getAmount()))
                                return Mono.error(new MovementCreationException("Not enough money"));
                            if (!existingAccount.isMovementInAccountLimit(movementsCurrentMonth))
                                return Mono.error(new MovementCreationException("Reach limit of movements per month"));
                            existingAccount.makeMovement(movement.getType(), movement.getAmount());
                            return accountRepository.save(existingAccount);
                        }))
                .then(Mono.just(movementRequest))
                .map(MovementRequest::toModel)
                .flatMap(movement -> movementRepository.insert(movement))
                .map(MovementResponse::fromModel)
                .onErrorMap(ex -> new MovementCreationException(ex.getMessage()));
    }

    @Override
    public Mono<MovementResponse> update(String id, MovementRequest movementRequest) {

        Mono<Movement> movementMono = movementRepository.findById(id)
                .switchIfEmpty(Mono.error(new MovementCreationException("Movement not found with id: " + movementRequest.getAccount())))
                .flatMap(existingMovement -> {
                    Movement differenceMovement =
                            MovementUtils.createDifferenceMovement(existingMovement, movementRequest);
                    return Mono.just(differenceMovement);
                });

        Mono<Account> accountMono = accountRepository.findById(movementRequest.getAccount())
                .switchIfEmpty(Mono.error(new MovementCreationException("Account not found with id: " + movementRequest.getAccount())));

        return movementMono.zipWith(accountMono)
                .flatMap(result -> {
                    Movement differenceMovement = result.getT1();
                    Account accountFound = result.getT2();
                    if (accountFound.isMovementValid(differenceMovement.getType(), differenceMovement.getAmount())) {
                        accountFound.makeMovement(differenceMovement.getType(), differenceMovement.getAmount());
                        return accountRepository.save(accountFound);
                    } else {
                        return Mono.error(new MovementCreationException("Not enough money"));
                    }
                })
                .then(Mono.just(movementRequest))
                .map(MovementRequest::toModel)
                .map(movement -> {
                    movement.setId(id);
                    return movement;
                })
                .flatMap(movement -> movementRepository.save(movement))
                .map(MovementResponse::fromModel)
                .onErrorMap(ex -> new MovementCreationException(ex.getMessage()));

    }

}
