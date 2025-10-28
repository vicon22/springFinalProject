package com.eveiled.hotelservice.service;

import com.eveiled.hotelservice.dto.AvailabilityRequest;
import com.eveiled.hotelservice.dto.RoomDto;
import com.eveiled.hotelservice.entity.Room;
import com.eveiled.hotelservice.mapper.HotelMapper;
import com.eveiled.hotelservice.repository.RoomRepository;
import com.eveiled.hotelservice.util.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final HotelService hotelService;
    private final HotelMapper hotelMapper;
    
    public RoomDto createRoom(RoomDto roomDto) {
        Room room = new Room();
        room.setNumber(roomDto.getNumber());
        room.setAvailable(roomDto.getAvailable());
        room.setTimesBooked(roomDto.getTimesBooked() != null ? roomDto.getTimesBooked() : 0);
        room.setHotel(hotelMapper.toEntity(hotelService.getHotelById(roomDto.getHotelId())));
        Room savedRoom = roomRepository.save(room);
        return hotelMapper.toRoomDto(savedRoom);
    }
    
    @Transactional(readOnly = true)
    public List<RoomDto> getAllAvailableRooms() {
        List<Room> rooms = roomRepository.findByAvailableTrue();
        return hotelMapper.toRoomDtoList(rooms);
    }
    
    @Transactional(readOnly = true)
    public List<RoomDto> getRecommendedRooms() {
        List<Room> rooms = roomRepository.findAvailableRoomsOrderedByTimesBooked();
        return hotelMapper.toRoomDtoList(rooms);
    }
    
    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRoomsNotBlocked() {
        List<Room> rooms = roomRepository.findAvailableRoomsNotBlocked(LocalDateTime.now());
        return hotelMapper.toRoomDtoList(rooms);
    }
    
    public boolean confirmAvailability(Long roomId, AvailabilityRequest request) {
        CorrelationIdUtil.setRoomId(roomId);
        String correlationId = CorrelationIdUtil.getCorrelationId();
        
        log.info("HOTEL_AVAILABILITY_CHECK: Confirming availability for room {} with requestId {} [roomId={}, correlationId={}]", 
                roomId, request.getRequestId(), roomId, correlationId);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
        
        if (!room.getAvailable()) {
            log.warn("HOTEL_ROOM_UNAVAILABLE: Room {} is not available [roomId={}, correlationId={}]", 
                    roomId, roomId, correlationId);
            return false;
        }

        //Поддержка идемпотентности
        if (room.getBlockedUntil() != null && room.getBlockedUntil().isAfter(LocalDateTime.now())) {
            if (request.getRequestId().equals(room.getBlockedByRequestId())) {
                log.info("HOTEL_ROOM_ALREADY_BLOCKED_BY_SAME_REQUEST: Room {} already blocked by same requestId {} - idempotent operation [roomId={}, correlationId={}]", 
                        roomId, request.getRequestId(), roomId, correlationId);
                return true;
            } else {
                log.warn("HOTEL_ROOM_BLOCKED: Room {} is already blocked until {} by different request {} [roomId={}, correlationId={}]", 
                        roomId, room.getBlockedUntil(), room.getBlockedByRequestId(), roomId, correlationId);
                return false;
            }
        }

        room.setBlockedUntil(request.getEndDate().plusHours(1));
        room.setBlockedByRequestId(request.getRequestId());
        roomRepository.save(room);
        
        log.info("HOTEL_ROOM_BLOCKED_SUCCESS: Room {} blocked successfully for request {} until {} [roomId={}, correlationId={}]", 
                roomId, request.getRequestId(), room.getBlockedUntil(), roomId, correlationId);
        return true;
    }
    
    public void releaseRoom(Long roomId, String requestId) {
        CorrelationIdUtil.setRoomId(roomId);
        String correlationId = CorrelationIdUtil.getCorrelationId();
        
        log.info("HOTEL_ROOM_RELEASE: Releasing room {} for requestId {} [roomId={}, correlationId={}]", 
                roomId, requestId, roomId, correlationId);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
        
        if (requestId.equals(room.getBlockedByRequestId())) {
            room.setBlockedUntil(null);
            room.setBlockedByRequestId(null);
            roomRepository.save(room);
            log.info("HOTEL_ROOM_RELEASE_SUCCESS: Room {} released successfully for request {} [roomId={}, correlationId={}]", 
                    roomId, requestId, roomId, correlationId);
        } else {
            log.warn("HOTEL_ROOM_RELEASE_MISMATCH: Request {} does not match blocked request {} for room {} [roomId={}, correlationId={}]", 
                    requestId, room.getBlockedByRequestId(), roomId, roomId, correlationId);
        }
    }
    
    public void incrementTimesBooked(Long roomId) {
        CorrelationIdUtil.setRoomId(roomId);
        String correlationId = CorrelationIdUtil.getCorrelationId();
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (room.getBlockedUntil() != null && room.getBlockedByRequestId() != null) {
            int previousTimesBooked = room.getTimesBooked();
            room.setTimesBooked(room.getTimesBooked() + 1);
            room.setBlockedUntil(null);
            room.setBlockedByRequestId(null);
            roomRepository.save(room);
            
            log.info("HOTEL_ROOM_BOOKING_FINALIZED: Incremented times booked for room {} from {} to {} [roomId={}, correlationId={}]", 
                    roomId, previousTimesBooked, room.getTimesBooked(), roomId, correlationId);
        } else {
            log.info("HOTEL_ROOM_BOOKING_ALREADY_FINALIZED: Room {} booking already finalized - idempotent operation [roomId={}, correlationId={}]", 
                    roomId, roomId, correlationId);
        }
    }
    
    public void releaseRoomsByRequestId(String requestId) {
        log.info("Releasing all rooms for requestId {}", requestId);
        
        List<Room> blockedRooms = roomRepository.findByBlockedByRequestId(requestId);
        for (Room room : blockedRooms) {
            room.setBlockedUntil(null);
            room.setBlockedByRequestId(null);
            roomRepository.save(room);
        }
        
        log.info("Released {} rooms for request {}", blockedRooms.size(), requestId);
    }
}
