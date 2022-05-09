package com.group7.accountsservice.model;

import com.group7.accountsservice.exception.movement.MovementCreationException;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    @NonNull
    private String client;
    private String clientType;
    @NonNull
    private String type;
    @NonNull
    private Double balance;
    private Double maintenanceFee;
    private Integer movementsLimit;
    private List<String> holders;
    private List<String> signers;
    private Integer movementDay;

    public boolean isMovementValid(String type, Double amount) {

        if (Objects.isNull(type) || Objects.isNull(amount))
            throw new MovementCreationException("Type, Account and Amount are mandatory attributes");

        return !type.equalsIgnoreCase("withdraw") ||
                balance >= amount;
    }

    public boolean isMovementInAccountLimit(Long count) {
        int countInt = count.intValue();

        LocalDate currentDate = LocalDate.now();
        if (type.equals("Fixed Deposit") && currentDate.getDayOfMonth()!=movementDay){
            return false;
        }

        return countInt<movementsLimit;
    }

    public void makeMovement(String type, Double amount) {
        if (type.equalsIgnoreCase("withdraw")) {
            balance -= amount;
        } else if (type.equalsIgnoreCase("deposit")) {
            balance += amount;
        }
    }


}
