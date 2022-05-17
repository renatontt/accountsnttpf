package com.group7.accountsservice.controller;

import com.group7.accountsservice.dto.AccountRequest;
import com.group7.accountsservice.dto.AccountResponse;
import com.group7.accountsservice.dto.FeeResponse;
import com.group7.accountsservice.service.AccountService;
import com.group7.accountsservice.service.MovementService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
@Slf4j
public class AccountController {
    private AccountService service;

    private MovementService movementService;

    @GetMapping
    public Flux<AccountResponse> getAllAccounts() {
        return service.getAll();
    }

    @GetMapping("/client/{client}")
    public Flux<AccountResponse> getAllAccountsByClient(@PathVariable String client) {
        return service.getAllByClient(client);
    }

    @GetMapping("{id}")
    public Mono<AccountResponse> getAccount(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("{id}/dailyBalance")
    public Mono<Double> getReportOfDailyBalance(@PathVariable String id) {
        return movementService.getReportOfDailyBalance(id);
    }

    @GetMapping("{id}/feeReport/from/{yearFrom}/{monthFrom}/{dayFrom}/to/{yearTo}/{monthTo}/{dayTo}")
    public Flux<FeeResponse> getAllFeesByAccountAndPeriod(@PathVariable String id, @PathVariable Integer yearFrom, @PathVariable Integer monthFrom,
                                                          @PathVariable Integer dayFrom, @PathVariable Integer yearTo, @PathVariable Integer monthTo, @PathVariable Integer dayTo) {

        LocalDate from = LocalDate.of(yearFrom,monthFrom,dayFrom);
        LocalDate to = LocalDate.of(yearTo,monthTo,dayTo);

        return movementService.getAllFeesByAccountAndPeriod(id,from,to);
    }

    @GetMapping("/client/{client}/dailyBalance")
    public Flux<Map<String, Double>> getAllAccountsReportByClient(@PathVariable String client) {
        return movementService.getAllReportsByClient(client);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AccountResponse> saveAccount(@RequestBody AccountRequest accountRequest) {
        return service.save(accountRequest);
    }

    @PutMapping("{id}")
    public Mono<AccountResponse> updateAccount(@PathVariable String id,
                                               @RequestBody AccountRequest accountRequest) {
        return service.update(id, accountRequest);
    }

    @DeleteMapping("{id}")
    public Mono<Void> deleteAccount(@PathVariable String id) {
        return service.delete(id);
    }

    @DeleteMapping
    public Mono<Void> deleteAllAccounts() {
        return service.deleteAll();
    }
}
