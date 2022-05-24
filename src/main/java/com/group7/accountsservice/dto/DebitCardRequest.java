package com.group7.accountsservice.dto;

import com.group7.accountsservice.model.DebitCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class DebitCardRequest {
    private String number;
    private String client;
    private String account;

    public DebitCard toModel(){
        return DebitCard.builder()
                .number(this.number)
                .client(this.client)
                .mainAccount(this.account)
                .optionalAccounts(new ArrayList<>(Arrays.asList(this.account)))
                .build();
    }
}
