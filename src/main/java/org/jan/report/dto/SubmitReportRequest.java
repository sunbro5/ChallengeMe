package org.jan.report.dto;

import lombok.Data;

@Data
public class SubmitReportRequest {
    private String reportedUsername;
    private String reason;
}
