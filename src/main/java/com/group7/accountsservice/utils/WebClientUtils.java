package com.group7.accountsservice.utils;

import com.group7.accountsservice.exception.account.AccountCreationException;
import com.group7.accountsservice.model.Client;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class WebClientUtils {
    private WebClient webClient;

    public WebClientUtils() {
        this.webClient = WebClient.create("http://localhost:8080/clients");
    }

    public WebClientUtils(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Client> getClient(String id) {
        return webClient
                .get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new AccountCreationException("Not found Client with ID: " + id)))
                .bodyToMono(Client.class);
    }

}
