package ru.variousvar.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
public class Transaction {
    private Long id;
    private Long sender;
    private Long receiver;
    private long amount;
    private String description;
    private Date created;

    @JsonGetter("created")
    public String getFormattedDate() {
        return created == null ? null : created.toInstant().toString();
    }
}
