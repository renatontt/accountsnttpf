package com.group7.accountsservice.utils;

import com.group7.accountsservice.configuration.CurrentAccountConfiguration;
import com.group7.accountsservice.configuration.FixedDepositAccountConfiguration;
import com.group7.accountsservice.configuration.SavingAccountConfiguration;
import com.group7.accountsservice.model.Account;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class AccountUtils {

    private CurrentAccountConfiguration currentAccountConfiguration;
    private FixedDepositAccountConfiguration fixedDepositAccountConfiguration;
    private SavingAccountConfiguration savingAccountConfiguration;

    public void setMaintenanceFee(Account account){
        switch (account.getType()){
            case "Current":
                if (account.getClientType().equalsIgnoreCase("PYME")){
                    account.setMaintenanceFee(0.0);
                }else{
                    account.setMaintenanceFee(currentAccountConfiguration.getMaintenanceFee());
                }
                break;
            case "Saving":
                account.setMaintenanceFee(savingAccountConfiguration.getMaintenanceFee());
                break;
            case "Fixed Deposit":
                account.setMaintenanceFee(fixedDepositAccountConfiguration.getMaintenanceFee());
                break;
            default:
                break;
        }
    }

    public void setMovementsLimit(Account account){
        switch (account.getType()){
            case "Current":
                account.setMovementsLimit(Integer.MAX_VALUE);
                break;
            case "Saving":
                account.setMovementsLimit(savingAccountConfiguration.getMovementsLimit());
                break;
            case "Fixed Deposit":
                account.setMovementsLimit(fixedDepositAccountConfiguration.getMovementsLimit());
                break;
            default:
                break;
        }
    }


}
