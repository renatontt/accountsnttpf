package com.group7.accountsservice.it;

import com.group7.accountsservice.dto.AccountResponse;
import com.group7.accountsservice.exception.ExceptionResponse;
import com.group7.accountsservice.model.Account;
import com.group7.accountsservice.repository.AccountRepository;
import com.group7.accountsservice.utils.AccountUtils;
import com.group7.accountsservice.utils.WebClientUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountIT {
    private static final String ACCOUNT_ID = "627760015d3f4d6ace96c44b";
    private static final String ACCOUNT_TYPE = "Saving";
    private static final String ACCOUNT_CLIENT = "627718aff4256e7261ae367f";
    private static final Double ACCOUNT_BALANCE = 100.0;
    private static final String ACCOUNT_CLIENT_TYPE = "Personal";
    private static final Double ACCOUNT_MAINTENANCE_FEE = 0.0;
    private static final Integer ACCOUNT_MOVEMENTS_LIMIT = 5;
    private static final List<String> ACCOUNT_HOLDERS = null;
    private static final List<String> ACCOUNT_SIGNERS = null;
    private static final Integer ACCOUNT_MOVEMENT_DAY = null;
    private static final String ACCOUNT_CLIENT_NAME = "Renato";
    private static final String ACCOUNT_CLIENT_DOCUMENT_TYPE = "DNI";
    private static final Long ACCOUNT_CLIENT_DOCUMENT_NUMBER = 71318990L;

    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private AccountUtils accountUtils;
    @MockBean
    private WebClientUtils webClientUtils;

    @Autowired
    WebTestClient client;

    @Test
    void shouldGetOneAccount(){
        //GIVEN
        Account account = Account.builder()
                .id(ACCOUNT_ID)
                .type(ACCOUNT_TYPE)
                .client(ACCOUNT_CLIENT)
                .balance(ACCOUNT_BALANCE)
                .clientType(ACCOUNT_CLIENT_TYPE)
                .maintenanceFee(ACCOUNT_MAINTENANCE_FEE)
                .movementsLimit(ACCOUNT_MOVEMENTS_LIMIT)
                .holders(ACCOUNT_HOLDERS)
                .signers(ACCOUNT_SIGNERS)
                .movementDay(ACCOUNT_MOVEMENT_DAY)
                .build();

        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Mono.just(account));
        //WHEN
        AccountResponse accountResponse = client.get()
                .uri(uriBuilder -> uriBuilder.path("/accounts/{id}").build(ACCOUNT_ID))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountResponse.class)
                .returnResult().getResponseBody();
        //THEN
        assertNotNull(accountResponse);
        assertEquals(ACCOUNT_ID, accountResponse.getId());
        assertEquals(ACCOUNT_TYPE, accountResponse.getType());
        assertEquals(ACCOUNT_CLIENT, accountResponse.getClient());
        assertEquals(ACCOUNT_BALANCE, accountResponse.getBalance());
        assertEquals(ACCOUNT_CLIENT_TYPE, accountResponse.getClientType());
        assertEquals(ACCOUNT_MAINTENANCE_FEE, accountResponse.getMaintenanceFee());
        assertEquals(ACCOUNT_MOVEMENTS_LIMIT, accountResponse.getMovementsLimit());
        assertEquals(ACCOUNT_HOLDERS, accountResponse.getHolders());
        assertEquals(ACCOUNT_SIGNERS, accountResponse.getSigners());
        assertEquals(ACCOUNT_MOVEMENT_DAY, accountResponse.getMovementDay());
    }

    @Test
    void shouldReturnNotFoundWhenGetOneAccount(){
        //GIVEN
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Mono.empty());
        //WHEN
        ExceptionResponse exceptionResponse = client.get()
                .uri(uriBuilder -> uriBuilder.path("/accounts/{id}").build(ACCOUNT_ID))
                .exchange()
        //THEN
                .expectStatus().isNotFound()
                .expectBody(ExceptionResponse.class)
                .returnResult().getResponseBody();

        //assertTrue(exceptionResponse.getMessage().contains(""));
    }
}
