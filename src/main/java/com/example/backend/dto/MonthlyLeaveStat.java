package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyLeaveStat {
    private String month;
    private long requestCount;
}
