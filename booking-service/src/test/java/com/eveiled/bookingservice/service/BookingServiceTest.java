package com.eveiled.bookingservice.service;

import com.eveiled.bookingservice.dto.BookingDto;
import com.eveiled.bookingservice.dto.CreateBookingRequest;
import com.eveiled.bookingservice.dto.RoomDto;
import com.eveiled.bookingservice.entity.Booking;
import com.eveiled.bookingservice.entity.User;
import com.eveiled.bookingservice.mapper.BookingMapper;
import com.eveiled.bookingservice.repository.BookingRepository;
import com.eveiled.bookingservice.util.CorrelationIdUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private CreateBookingRequest createBookingRequest;
    private Booking booking;
    private Booking savedBooking;
    private BookingDto bookingDto;
    private RoomDto roomDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookingService, "hotelServiceUrl", "http://localhost:8081");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(User.Role.USER);

        createBookingRequest = new CreateBookingRequest();
        createBookingRequest.setRoomId(1L);
        createBookingRequest.setStartDate(LocalDateTime.now().plusDays(1));
        createBookingRequest.setEndDate(LocalDateTime.now().plusDays(3));
        createBookingRequest.setAutoSelect(false);

        booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoomId(1L);
        booking.setStartDate(createBookingRequest.getStartDate());
        booking.setEndDate(createBookingRequest.getEndDate());
        booking.setStatus(Booking.Status.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setRequestId("test-request-id");

        savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setUser(user);
        savedBooking.setRoomId(1L);
        savedBooking.setStartDate(createBookingRequest.getStartDate());
        savedBooking.setEndDate(createBookingRequest.getEndDate());
        savedBooking.setStatus(Booking.Status.CONFIRMED);
        savedBooking.setCreatedAt(LocalDateTime.now());
        savedBooking.setRequestId("test-request-id");

        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setRoomId(1L);
        bookingDto.setStartDate(createBookingRequest.getStartDate());
        bookingDto.setEndDate(createBookingRequest.getEndDate());
        bookingDto.setStatus(Booking.Status.CONFIRMED);

        roomDto = new RoomDto();
        roomDto.setId(1L);
        roomDto.setNumber("101");
        roomDto.setAvailable(true);
        roomDto.setTimesBooked(5);
        roomDto.setHotelId(1L);
    }

    @Test
    void createBooking_WhenRoomIsAvailable_ShouldReturnConfirmedBooking() {
        try (MockedStatic<CorrelationIdUtil> mockedStatic = mockStatic(CorrelationIdUtil.class)) {
            mockedStatic.when(CorrelationIdUtil::getCorrelationId).thenReturn("test-correlation-id");
            mockedStatic.when(() -> CorrelationIdUtil.setBookingId(anyLong())).thenAnswer(invocation -> null);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingMapper.toDto(savedBooking)).thenReturn(bookingDto);

            BookingDto result = bookingService.createBooking(createBookingRequest, "testuser");

            assertNotNull(result);
            assertEquals(Booking.Status.CONFIRMED, result.getStatus());
            assertEquals(1L, result.getRoomId());

            verify(userService).findByUsername("testuser");
            verify(bookingRepository, times(2)).save(any(Booking.class));
            verify(bookingMapper).toDto(savedBooking);
        }
    }

    @Test
    void createBooking_WhenRoomIsNotAvailable_ShouldReturnCancelledBooking() {
        try (MockedStatic<CorrelationIdUtil> mockedStatic = mockStatic(CorrelationIdUtil.class)) {
            mockedStatic.when(CorrelationIdUtil::getCorrelationId).thenReturn("test-correlation-id");
            mockedStatic.when(() -> CorrelationIdUtil.setBookingId(anyLong())).thenAnswer(invocation -> null);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            BookingDto result = bookingService.createBooking(createBookingRequest, "testuser");

            assertNotNull(result);
            assertEquals(Booking.Status.CANCELLED, booking.getStatus());

            verify(userService).findByUsername("testuser");
            verify(bookingRepository, times(2)).save(any(Booking.class));
            verify(bookingMapper).toDto(booking);
        }
    }

    @Test
    void getUserBookings_ShouldReturnUserBookings() {
        List<Booking> bookings = Arrays.asList(savedBooking);
        List<BookingDto> bookingDtos = Arrays.asList(bookingDto);

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(bookingRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(bookings);
        when(bookingMapper.toDtoList(bookings)).thenReturn(bookingDtos);

        List<BookingDto> result = bookingService.getUserBookings("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingDto.getId(), result.get(0).getId());

        verify(userService).findByUsername("testuser");
        verify(bookingRepository).findByUserOrderByCreatedAtDesc(user);
        verify(bookingMapper).toDtoList(bookings);
    }

    @Test
    void getBookingById_WhenBookingExists_ShouldReturnBooking() {
        Long bookingId = 1L;
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(bookingRepository.findByIdAndUser(bookingId, user)).thenReturn(Optional.of(savedBooking));
        when(bookingMapper.toDto(savedBooking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(bookingId, "testuser");

        assertNotNull(result);
        assertEquals(bookingDto.getId(), result.getId());

        verify(userService).findByUsername("testuser");
        verify(bookingRepository).findByIdAndUser(bookingId, user);
        verify(bookingMapper).toDto(savedBooking);
    }

    @Test
    void cancelBooking_WhenBookingIsConfirmed_ShouldCancelAndReleaseRoom() {
        Long bookingId = 1L;
        try (MockedStatic<CorrelationIdUtil> mockedStatic = mockStatic(CorrelationIdUtil.class)) {
            mockedStatic.when(CorrelationIdUtil::getCorrelationId).thenReturn("test-correlation-id");
            mockedStatic.when(() -> CorrelationIdUtil.setBookingId(anyLong())).thenAnswer(invocation -> null);

            when(userService.findByUsername("testuser")).thenReturn(user);
            when(bookingRepository.findByIdAndUser(bookingId, user)).thenReturn(Optional.of(savedBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

            bookingService.cancelBooking(bookingId, "testuser");

            assertEquals(Booking.Status.CANCELLED, savedBooking.getStatus());
            verify(userService).findByUsername("testuser");
            verify(bookingRepository).findByIdAndUser(bookingId, user);
            verify(bookingRepository).save(savedBooking);
        }
    }

}