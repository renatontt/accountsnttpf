package com.group7.accountsservice.model;

import com.group7.accountsservice.exception.movement.MovementCreationException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    @NonNull
    private String client;
    private String clientType;
    private String clientProfile;
    @NonNull
    private String type;
    @NonNull
    private Double balance;
    private Double maintenanceFee;
    private Integer movementsLimit;
    private List<String> holders;
    private List<String> signers;
    private Integer movementDay;

    public boolean isMovementValid(final Movement movement) {

        if (Objects.isNull(movement.getType()) || Objects.isNull(movement.getAmount())) {
            throw new MovementCreationException("Type, Account and Amount are mandatory attributes");
        }

        return !movement.getType().equalsIgnoreCase("withdraw") ||
                balance >= movement.getAmount() + movement.getTransactionFee();
    }

    public boolean canFixedAccountMove() {
        return !type.equals("Fixed Deposit")
                || LocalDate.now().getDayOfMonth() == movementDay;
    }

    public boolean isMovementInAccountLimit(final Long count) {
        return count.intValue() < movementsLimit;
    }

    public void makeMovement(final Movement movement) {
        if (movement.getType().equalsIgnoreCase("withdraw")) {
            balance -= movement.getAmount();
        } else if (movement.getType().equalsIgnoreCase("deposit")) {
            balance += movement.getAmount();
        }

        if (!Objects.isNull(movement.getTransactionFee())) {
            balance -= movement.getTransactionFee();
        }
    }


}
