package com.group7.accountsservice.utils;

import com.group7.accountsservice.dto.MovementRequest;
import com.group7.accountsservice.model.Movement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class MovementUtils {

    public Movement createDifferenceMovement(Movement current, MovementRequest update){
        String type;

        double difference = update.getAmountSigned() - current.getAmountSigned();
        if (difference>=0){
            type="deposit";
        }else{
            type="withdraw";
        }
        return new Movement(current.getId(), type, current.getTransactionFee(), Math.abs(difference), current.getDate(), current.getAccount());
    }

    public void setTransactionFee(Movement movement, String accountType){
        log.info("Account type: {}",accountType);
        switch (accountType){
            case "Current":
                movement.setTransactionFee(0.0);
                break;
            case "Saving":
                movement.setTransactionFee(5.0);
                break;
            case "Fixed Deposit":
                movement.setTransactionFee(4.0);
                break;
            default:
                break;
        }
    }

}
