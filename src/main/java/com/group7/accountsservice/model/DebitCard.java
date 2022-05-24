package com.group7.accountsservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Document(collection="debit_cards")
public class DebitCard {
    @Id
    private String id;
    private String number;
    private String client;
    private String mainAccount;
    private ArrayList<String> optionalAccounts;
}
