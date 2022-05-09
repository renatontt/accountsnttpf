package com.group7.accountsservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Document(collection="movements")
public class Movement {
    @Id
    private String id;
    @NonNull
    private String type;
    @NonNull
    private Double amount;
    private Date date;
    @NonNull
    private String account;

    public Double getAmountSigned(){
        return type.equals("withdraw")?-1*amount:amount;
    }
}
