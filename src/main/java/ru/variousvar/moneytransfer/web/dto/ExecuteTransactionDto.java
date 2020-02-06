package ru.variousvar.moneytransfer.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ExecuteTransactionDto {
    private Long sender;
    private Long receiver;
    private Long amount;
    private String description;
}
