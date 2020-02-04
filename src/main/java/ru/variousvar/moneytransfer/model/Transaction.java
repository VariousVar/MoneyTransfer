package ru.variousvar.moneytransfer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
public class Transaction {
    private Long id;
    private Account fromAccount; // fixme actually, need only id and name, current balance is irrelevant within transaction
    private Account toAccount;
    private long amount; // todo use BigDecimal
    private String description;
    private Date created;
}
