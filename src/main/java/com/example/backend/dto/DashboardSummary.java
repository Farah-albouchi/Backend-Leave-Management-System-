package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardSummary {
    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
}
