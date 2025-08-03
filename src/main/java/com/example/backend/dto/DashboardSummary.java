package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
}
