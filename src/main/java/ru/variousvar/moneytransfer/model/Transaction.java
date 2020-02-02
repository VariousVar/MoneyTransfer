package ru.variousvar.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class Transaction {
    private Long id;
    private Account fromAccount;
    private Account toAccount;
    private long amount; // todo use BigDecimal
    private String description;
    private Date created;
}
