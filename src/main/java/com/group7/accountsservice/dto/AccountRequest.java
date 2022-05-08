package com.group7.accountsservice.dto;

import com.group7.accountsservice.exception.account.AccountCreationException;
import com.group7.accountsservice.model.Account;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class AccountRequest {

    private String id;
    private String client;
    private String clientType;
    private String type;
    private Double balance;
    private List<String> holders;
    private List<String> signers;
    private Integer movementDay;

    public Account toModel() {

        if (Objects.isNull(type) || Objects.isNull(balance) || Objects.isNull(client))
            throw new AccountCreationException("Type, Balance and Client are mandatory attributes");

        if (!type.equals("Saving") && !type.equals("Current") && !type.equals("Fixed Deposit"))
            throw new AccountCreationException("The type of account must have a value from: 'Saving','Current' or 'Fixed Deposit'");

        if (balance < 0)
            throw new AccountCreationException("The balance could not be less than 0 or null");

        if (clientType.equals("Business") &&
                (type.equals("Saving") || type.equals("Fixed Deposit") || Objects.isNull(holders) || holders.isEmpty()))
            throw new AccountCreationException("A business account can only be of 'Current' type and must have at least one account holder");

        if (type.equals("Fixed Deposit") && Objects.isNull(movementDay))
            throw new AccountCreationException("If account type is 'Fixed Deposit' must have a Movement Day attribute");

        return Account.builder()
                .client(this.client)
                .clientType(this.clientType)
                .type(this.type)
                .balance(this.balance)
                .holders(this.holders)
                .signers(this.signers)
                .movementDay(this.movementDay)
                .build();
    }

}
