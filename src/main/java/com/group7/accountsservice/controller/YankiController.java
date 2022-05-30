package com.group7.accountsservice.controller;

import com.group7.accountsservice.dto.AccountResponse;
import com.group7.accountsservice.dto.Yanki;
import com.group7.accountsservice.serviceimpl.MessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@AllArgsConstructor
@Slf4j
public class YankiController {

    private MessageService service;

    @GetMapping("/toyanki")
    public Mono<Boolean> getAllAccounts() {
        return Mono.just(service.sendToYanki(new Yanki("id",9L,10L,5.0, LocalDate.now())));
    }

    @GetMapping("/toyanki2")
    public boolean getAllAccounts2() {
        return true;
    }

}
