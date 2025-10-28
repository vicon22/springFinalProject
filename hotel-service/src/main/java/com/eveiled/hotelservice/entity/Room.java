package com.eveiled.hotelservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    
    @Column(nullable = false)
    private String number;
    
    @Column(nullable = false)
    private Boolean available = true;
    
    @Column(nullable = false)
    private Integer timesBooked = 0;
    
    @Column
    private LocalDateTime blockedUntil;
    
    @Column
    private String blockedByRequestId;
}
