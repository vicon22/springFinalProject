package com.eveiled.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    private Long roomId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean autoSelect = false;
}
