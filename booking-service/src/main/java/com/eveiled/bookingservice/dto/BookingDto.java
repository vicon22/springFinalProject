package com.eveiled.bookingservice.dto;

import com.eveiled.bookingservice.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private Long userId;
    private Long roomId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Booking.Status status;
    private LocalDateTime createdAt;
}
