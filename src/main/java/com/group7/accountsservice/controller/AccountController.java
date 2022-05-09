package com.group7.accountsservice.controller;

import com.group7.accountsservice.dto.AccountRequest;
import com.group7.accountsservice.dto.AccountResponse;
import com.group7.accountsservice.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
@Slf4j
public class AccountController {
    private AccountService service;

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
