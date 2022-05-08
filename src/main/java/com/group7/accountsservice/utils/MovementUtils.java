package com.group7.accountsservice.utils;

import com.group7.accountsservice.dto.MovementRequest;
import com.group7.accountsservice.model.Movement;

public class MovementUtils {

    public static Movement createDifferenceMovement(Movement current, MovementRequest update){
        String type = "";

        Double difference = update.getAmountSigned() - current.getAmountSigned();
        if (difference>=0){
            type="deposit";
        }else{
            type="withdraw";
        }
        return new Movement(current.getId(), type, Math.abs(difference), current.getDate(), current.getAccount());
    }

}
