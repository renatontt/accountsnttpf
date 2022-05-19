package com.group7.accountsservice.serviceimpl;

import com.group7.accountsservice.dto.AccountRequest;
import com.group7.accountsservice.exception.account.AccountCreationException;
import com.group7.accountsservice.model.Account;
import com.group7.accountsservice.model.Client;
import com.group7.accountsservice.repository.AccountRepository;
import com.group7.accountsservice.utils.AccountUtils;
import com.group7.accountsservice.utils.WebClientUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

class AccountServiceImplTest {

    private static final String ACCOUNT_ID = "627760015d3f4d6ace96c44b";
    private static final String ACCOUNT_TYPE = "Saving";
    private static final String ACCOUNT_CLIENT = "627718aff4256e7261ae367f";
    private static final Double ACCOUNT_BALANCE = 100.0;
    private static final String ACCOUNT_CLIENT_TYPE = "Personal";
    private static final String ACCOUNT_CLIENT_PROFILE = "";
    private static final Double ACCOUNT_MAINTENANCE_FEE = 0.0;
    private static final Integer ACCOUNT_MOVEMENTS_LIMIT = 5;
    private static final List<String> ACCOUNT_HOLDERS = null;
    private static final List<String> ACCOUNT_SIGNERS = null;
    private static final Integer ACCOUNT_MOVEMENT_DAY = null;
    private static final String ACCOUNT_CLIENT_NAME = "Renato";
    private static final String ACCOUNT_CLIENT_DOCUMENT_TYPE = "DNI";
    private static final Long ACCOUNT_CLIENT_DOCUMENT_NUMBER = 71318990L;


    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUtils accountUtils;
    @Mock
    private WebClientUtils webClientUtils;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateAccount() {
        //GIVEN
        AccountRequest accountRequest = AccountRequest.builder()
                .type(ACCOUNT_TYPE)
                .client(ACCOUNT_CLIENT)
                .balance(ACCOUNT_BALANCE)
                .build();

        Account account = Account.builder()
                .id(ACCOUNT_ID)
                .type(ACCOUNT_TYPE)
                .client(ACCOUNT_CLIENT)
                .balance(ACCOUNT_BALANCE)
                .clientType(ACCOUNT_CLIENT_TYPE)
                .clientProfile(ACCOUNT_CLIENT_PROFILE)
                .maintenanceFee(ACCOUNT_MAINTENANCE_FEE)
                .movementsLimit(ACCOUNT_MOVEMENTS_LIMIT)
                .holders(ACCOUNT_HOLDERS)
                .signers(ACCOUNT_SIGNERS)
                .movementDay(ACCOUNT_MOVEMENT_DAY)
                .build();

        Client client = Client.builder()
                .id(ACCOUNT_CLIENT)
                .name(ACCOUNT_CLIENT_NAME)
                .type(ACCOUNT_CLIENT_TYPE)
                .profile(ACCOUNT_CLIENT_PROFILE)
                .documentType(ACCOUNT_CLIENT_DOCUMENT_TYPE)
                .documentNumber(ACCOUNT_CLIENT_DOCUMENT_NUMBER).build();

        when(webClientUtils.getClient(any()))
                .thenReturn(Mono.just(client));

        when(accountRepository.findAccountByClientAndType(any(), any()))
                .thenReturn(Flux.empty());

        when(accountRepository.save(any()))
                .thenReturn(Mono.just(account));
        //WHEN
        StepVerifier.create(accountService.save(accountRequest))
                //THEN
                .assertNext(accountResponse -> {
                    assertNotNull(accountResponse);
                    assertEquals(ACCOUNT_ID, accountResponse.getId());
                    assertEquals(ACCOUNT_TYPE, accountResponse.getType());
                    assertEquals(ACCOUNT_CLIENT, accountResponse.getClient());
                    assertEquals(ACCOUNT_BALANCE, accountResponse.getBalance());
                    assertEquals(ACCOUNT_CLIENT_TYPE, accountResponse.getClientType());
                    assertEquals(ACCOUNT_CLIENT_PROFILE, accountResponse.getClientProfile());
                    assertEquals(ACCOUNT_MAINTENANCE_FEE, accountResponse.getMaintenanceFee());
                    assertEquals(ACCOUNT_MOVEMENTS_LIMIT, accountResponse.getMovementsLimit());
                    assertEquals(ACCOUNT_HOLDERS, accountResponse.getHolders());
                    assertEquals(ACCOUNT_SIGNERS, accountResponse.getSigners());
                    assertEquals(ACCOUNT_MOVEMENT_DAY, accountResponse.getMovementDay());
                })
                .verifyComplete();
    }

    @Test
    void shouldThrowAccountCreationExceptionWhenUnableToSave(){
        //GIVEN
        AccountRequest accountRequest = AccountRequest.builder()
                .type(ACCOUNT_TYPE)
                .client(ACCOUNT_CLIENT)
                .balance(ACCOUNT_BALANCE)
                .build();

        when(accountRepository.save(any()))
                .thenThrow(new RuntimeException("Connection Lost"));
        //WHEN
        StepVerifier.create(accountService.save(accountRequest))
        //THEN
                .verifyError(AccountCreationException.class);
    }


}
