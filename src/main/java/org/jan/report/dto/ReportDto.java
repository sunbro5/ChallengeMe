package org.jan.report.dto;

import lombok.Value;

@Value
public class ReportDto {
    Long id;
    String reporterUsername;
    String reportedUsername;
    Long reportedId;
    boolean reportedBanned;
    String reason;
    String createdAt;
    String status;
}
