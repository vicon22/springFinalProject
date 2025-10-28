package com.eveiled.bookingservice.service;

import com.eveiled.bookingservice.dto.AvailabilityRequest;
import com.eveiled.bookingservice.dto.BookingDto;
import com.eveiled.bookingservice.dto.CreateBookingRequest;
import com.eveiled.bookingservice.dto.RoomDto;
import com.eveiled.bookingservice.entity.Booking;
import com.eveiled.bookingservice.entity.User;
import com.eveiled.bookingservice.mapper.BookingMapper;
import com.eveiled.bookingservice.repository.BookingRepository;
import com.eveiled.bookingservice.util.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${hotel-service.url}")
    private String hotelServiceUrl;
    
    public BookingDto createBooking(CreateBookingRequest request, String username) {
        String correlationId = CorrelationIdUtil.getCorrelationId();
        log.info("BOOKING_PROCESS_START: Creating booking for user {} with request {} [correlationId={}]", 
                username, request, correlationId);
        
        User user = userService.findByUsername(username);
        String requestId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        
        // 1: создание брони со статусом PENDING
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoomId(request.getRoomId());
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setStatus(Booking.Status.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setRequestId(requestId);
        
        Booking savedBooking = bookingRepository.save(booking);

        CorrelationIdUtil.setBookingId(savedBooking.getId());
        
        log.info("BOOKING_CREATED: Created booking {} in PENDING status for room {} [bookingId={}, correlationId={}]", 
                savedBooking.getId(), request.getRoomId(), savedBooking.getId(), correlationId);
        
        try {
            // 2: Подтверждение доступности номера через Hotel Service
            log.info("AVAILABILITY_CHECK_START: Confirming availability for room {} with hotel service", request.getRoomId());
            boolean confirmed = confirmAvailabilityWithHotelService(request.getRoomId(), requestId, 
                    request.getStartDate(), request.getEndDate());
            
            if (confirmed) {
                log.info("AVAILABILITY_CONFIRMED: Room {} is available, updating booking to CONFIRMED", request.getRoomId());
                
                // 3: Обновить статус до CONFIRMED
                savedBooking.setStatus(Booking.Status.CONFIRMED);
                bookingRepository.save(savedBooking);
                
                // 4: Увеличить каунтер бронирований номера
                log.info("BOOKING_FINALIZING: Incrementing times booked for room {}", request.getRoomId());
                incrementTimesBookedInHotelService(request.getRoomId());
                
                log.info("BOOKING_PROCESS_SUCCESS: Booking {} confirmed successfully", savedBooking.getId());
            } else {
                log.warn("AVAILABILITY_DECLINED: Room {} is not available, cancelling booking", request.getRoomId());
                
                // 3: Обновить статус до CANCELLED в случае недоступности номер
                savedBooking.setStatus(Booking.Status.CANCELLED);
                bookingRepository.save(savedBooking);
                
                log.warn("BOOKING_PROCESS_CANCELLED: Booking {} cancelled due to unavailability", savedBooking.getId());
            }
        } catch (Exception e) {
            // 3: Обновить статус до CANCELLED в случае ошибки и убрать блокировку номера
            log.error("BOOKING_PROCESS_ERROR: Error confirming booking {}: {}", savedBooking.getId(), e.getMessage());
            
            savedBooking.setStatus(Booking.Status.CANCELLED);
            bookingRepository.save(savedBooking);

            log.info("ROOM_RELEASE: Releasing room {} due to booking error", request.getRoomId());
            releaseRoomInHotelService(request.getRoomId(), requestId);
            
            throw new RuntimeException("Failed to create booking: " + e.getMessage());
        }
        
        return bookingMapper.toDto(savedBooking);
    }
    
    public BookingDto createBookingWithAutoSelect(CreateBookingRequest request, String username) {
        log.info("Creating booking with auto-select for user {}", username);

        List<RoomDto> recommendedRooms = getRecommendedRoomsFromHotelService();
        
        if (recommendedRooms.isEmpty()) {
            throw new RuntimeException("No available rooms found");
        }

        RoomDto selectedRoom = recommendedRooms.get(0);
        request.setRoomId(selectedRoom.getId());
        
        return createBooking(request, username);
    }
    
    @Transactional(readOnly = true)
    public List<BookingDto> getUserBookings(String username) {
        User user = userService.findByUsername(username);
        List<Booking> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user);
        return bookingMapper.toDtoList(bookings);
    }
    
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long id, String username) {
        User user = userService.findByUsername(username);
        Booking booking = bookingRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return bookingMapper.toDto(booking);
    }
    
    public void cancelBooking(Long id, String username) {
        CorrelationIdUtil.setBookingId(id);
        String correlationId = CorrelationIdUtil.getCorrelationId();
        
        log.info("BOOKING_CANCELLATION_START: User {} cancelling booking {} [bookingId={}, correlationId={}]", 
                username, id, id, correlationId);
        
        User user = userService.findByUsername(username);
        Booking booking = bookingRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        if (booking.getStatus() == Booking.Status.CONFIRMED) {
            log.info("ROOM_RELEASE: Releasing room {} for cancelled booking", booking.getRoomId());
            releaseRoomInHotelService(booking.getRoomId(), booking.getRequestId());
        }
        
        booking.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);
        
        log.info("BOOKING_CANCELLATION_SUCCESS: Booking {} cancelled by user {} [bookingId={}, correlationId={}]", 
                id, username, id, correlationId);
    }
    
    private boolean confirmAvailabilityWithHotelService(Long roomId, String requestId, 
                                                      LocalDateTime startDate, LocalDateTime endDate) {
        try {
            AvailabilityRequest availabilityRequest = new AvailabilityRequest();
            availabilityRequest.setRequestId(requestId);
            availabilityRequest.setStartDate(startDate);
            availabilityRequest.setEndDate(endDate);
            
            Boolean result = webClientBuilder.build()
                    .post()
                    .uri(hotelServiceUrl + "/api/rooms/{id}/confirm-availability", roomId)
                    .bodyValue(availabilityRequest)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .retry(3)
                    .block();
            
            return result != null && result;
        } catch (Exception e) {
            log.error("Error confirming availability for room {}: {}", roomId, e.getMessage());
            return false;
        }
    }
    
    private void releaseRoomInHotelService(Long roomId, String requestId) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(hotelServiceUrl + "/api/rooms/{id}/release?requestId={requestId}", roomId, requestId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .retry(2)
                    .block();
        } catch (Exception e) {
            log.error("Error releasing room {}: {}", roomId, e.getMessage());
        }
    }
    
    private void incrementTimesBookedInHotelService(Long roomId) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(hotelServiceUrl + "/api/rooms/{id}/increment-bookings", roomId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .retry(2)
                    .block();
        } catch (Exception e) {
            log.error("Error incrementing times booked for room {}: {}", roomId, e.getMessage());
        }
    }
    
    private List<RoomDto> getRecommendedRoomsFromHotelService() {
        try {
            RoomDto[] rooms = webClientBuilder.build()
                    .get()
                    .uri(hotelServiceUrl + "/api/rooms/recommend")
                    .retrieve()
                    .bodyToMono(RoomDto[].class)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .retry(3)
                    .block();
            
            return rooms != null ? List.of(rooms) : List.of();
        } catch (Exception e) {
            log.error("Error getting recommended rooms: {}", e.getMessage());
            return List.of();
        }
    }
}
