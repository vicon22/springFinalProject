package com.eveiled.hotelservice.controller;

import com.eveiled.hotelservice.dto.AvailabilityRequest;
import com.eveiled.hotelservice.dto.RoomDto;
import com.eveiled.hotelservice.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room Management", description = "API for managing rooms")
public class RoomController {
    
    private final RoomService roomService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new room", description = "Only ADMIN can create rooms")
    public ResponseEntity<RoomDto> createRoom(@RequestBody RoomDto roomDto) {
        RoomDto createdRoom = roomService.createRoom(roomDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }
    
    @GetMapping
    @Operation(summary = "Get all available rooms", description = "Get list of all available rooms")
    public ResponseEntity<List<RoomDto>> getAllAvailableRooms() {
        List<RoomDto> rooms = roomService.getAllAvailableRooms();
        return ResponseEntity.ok(rooms);
    }
    
    @GetMapping("/recommend")
    @Operation(summary = "Get recommended rooms", description = "Get rooms sorted by times booked (ascending)")
    public ResponseEntity<List<RoomDto>> getRecommendedRooms() {
        List<RoomDto> rooms = roomService.getRecommendedRooms();
        return ResponseEntity.ok(rooms);
    }
    
    @PostMapping("/{id}/confirm-availability")
    @Operation(summary = "Confirm room availability", description = "Internal API for confirming room availability")
    public ResponseEntity<Boolean> confirmAvailability(@PathVariable Long id, @RequestBody AvailabilityRequest request) {
        boolean available = roomService.confirmAvailability(id, request);
        return ResponseEntity.ok(available);
    }
    
    @PostMapping("/{id}/release")
    @Operation(summary = "Release room", description = "Internal API for releasing room block")
    public ResponseEntity<Void> releaseRoom(@PathVariable Long id, @RequestParam String requestId) {
        roomService.releaseRoom(id, requestId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/increment-bookings")
    @Operation(summary = "Increment times booked", description = "Internal API for incrementing times booked")
    public ResponseEntity<Void> incrementTimesBooked(@PathVariable Long id) {
        roomService.incrementTimesBooked(id);
        return ResponseEntity.ok().build();
    }
}
