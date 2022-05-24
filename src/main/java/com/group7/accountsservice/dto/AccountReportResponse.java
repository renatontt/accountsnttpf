package com.group7.accountsservice.dto;

import com.group7.accountsservice.model.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountReportResponse {
    private String id;
    private String client;
    private String clientType;
    private String clientProfile;
    private String type;
    private Double balance;
    private Double maintenanceFee;
    private Integer movementsLimit;
    private List<String> holders;
    private List<String> signers;
    private Integer movementDay;
    private DebitCardResponse debitCard;

    private List<MovementResponse> movements;
    private List<FeeResponse> fees;
    private List<TransferResponse> transfers;


    public static AccountReportResponse fromModel(Account account) {
        return AccountReportResponse.builder()
                .id(account.getId())
                .client(account.getClient())
                .clientType(account.getClientType())
                .clientProfile(account.getClientProfile())
                .type(account.getType())
                .balance(account.getBalance())
                .maintenanceFee(account.getMaintenanceFee())
                .movementsLimit(account.getMovementsLimit())
                .movementDay(account.getMovementDay())
                .holders(account.getHolders())
                .build();
    }
}
