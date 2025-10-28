package com.eveiled.hotelservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRequest {
    private String requestId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
