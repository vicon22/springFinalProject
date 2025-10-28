package com.eveiled.hotelservice.repository;

import com.eveiled.hotelservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByAvailableTrue();
    
    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.timesBooked ASC, r.id ASC")
    List<Room> findAvailableRoomsOrderedByTimesBooked();
    
    @Query("SELECT r FROM Room r WHERE r.available = true AND (r.blockedUntil IS NULL OR r.blockedUntil < :now) ORDER BY r.timesBooked ASC, r.id ASC")
    List<Room> findAvailableRoomsNotBlocked(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM Room r WHERE r.blockedByRequestId = :requestId")
    List<Room> findByBlockedByRequestId(@Param("requestId") String requestId);
}
