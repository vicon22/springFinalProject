package com.eveiled.bookingservice.repository;

import com.eveiled.bookingservice.entity.Booking;
import com.eveiled.bookingservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<Booking> findByIdAndUser(Long id, User user);
    
    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId AND b.status = 'CONFIRMED' " +
           "AND ((b.startDate <= :endDate AND b.endDate >= :startDate))")
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.requestId = :requestId")
    List<Booking> findByRequestId(@Param("requestId") String requestId);
}
