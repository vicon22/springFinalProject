package com.eveiled.bookingservice.controller;

import com.eveiled.bookingservice.dto.BookingDto;
import com.eveiled.bookingservice.dto.CreateBookingRequest;
import com.eveiled.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "API for managing bookings")
public class BookingController {
    
    private final BookingService bookingService;
    
    @PostMapping
    @Operation(summary = "Create a new booking", description = "Create a new booking for authenticated user")
    public ResponseEntity<BookingDto> createBooking(@RequestBody CreateBookingRequest request, 
                                                   Authentication authentication) {
        String username = authentication.getName();
        BookingDto booking;
        
        if (request.getAutoSelect() != null && request.getAutoSelect()) {
            booking = bookingService.createBookingWithAutoSelect(request, username);
        } else {
            booking = bookingService.createBooking(request, username);
        }
        
        return ResponseEntity.ok(booking);
    }
    
    @GetMapping
    @Operation(summary = "Get user bookings", description = "Get all bookings for authenticated user")
    public ResponseEntity<List<BookingDto>> getUserBookings(Authentication authentication) {
        String username = authentication.getName();
        List<BookingDto> bookings = bookingService.getUserBookings(username);
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Get booking details by ID")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id, 
                                                   Authentication authentication) {
        String username = authentication.getName();
        BookingDto booking = bookingService.getBookingById(id, username);
        return ResponseEntity.ok(booking);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel booking", description = "Cancel a booking")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, 
                                            Authentication authentication) {
        String username = authentication.getName();
        bookingService.cancelBooking(id, username);
        return ResponseEntity.noContent().build();
    }
}
