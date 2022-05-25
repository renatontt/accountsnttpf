package com.group7.accountsservice.dto;

import com.group7.accountsservice.model.Transfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class TransferResponse {
    private String id;
    private String from;
    private String to;
    private Double amount;
    private LocalDate date;

    public static TransferResponse fromModel(Transfer transfer) {
        return TransferResponse.builder()
                .id(transfer.getId())
                .from(transfer.getFrom())
                .to(transfer.getTo())
                .amount(transfer.getAmount())
                .date(transfer.getDate())
                .build();

    }
}
