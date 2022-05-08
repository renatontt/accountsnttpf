package com.group7.accountsservice.dto;

import com.group7.accountsservice.model.Movement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovementResponse {
    private String id;
    private String type;
    private Double amount;
    private Date date;
    private String account;

    public static MovementResponse fromModel(Movement movement) {
        return MovementResponse.builder()
                .id(movement.getId())
                .type(movement.getType())
                .amount(movement.getAmount())
                .date(movement.getDate())
                .account(movement.getAccount())
                .build();

    }
}
