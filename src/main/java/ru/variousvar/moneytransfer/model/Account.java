package ru.variousvar.moneytransfer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Account {
    private Long id;
    private String name;
    private long balance; // todo use BigDecimal

}
