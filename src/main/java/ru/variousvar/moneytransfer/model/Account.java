package ru.variousvar.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Account {
    private Long id;
    private String name;
    private long balance; // todo use BigDecimal

}
