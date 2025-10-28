package com.eveiled.bookingservice.controller;

import com.eveiled.bookingservice.dto.BookingDto;
import com.eveiled.bookingservice.dto.CreateBookingRequest;
import com.eveiled.bookingservice.entity.Booking;
import com.eveiled.bookingservice.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser")
    void createBooking_WithManualRoomSelection_ShouldReturnBooking() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRoomId(1L);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(3));
        request.setAutoSelect(false);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setRoomId(1L);
        bookingDto.setStartDate(request.getStartDate());
        bookingDto.setEndDate(request.getEndDate());
        bookingDto.setStatus(Booking.Status.CONFIRMED);

        when(bookingService.createBooking(any(CreateBookingRequest.class), anyString()))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/api/bookings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).createBooking(any(CreateBookingRequest.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserBookings_ShouldReturnUserBookings() throws Exception {
        BookingDto booking1 = new BookingDto();
        booking1.setId(1L);
        booking1.setRoomId(1L);
        booking1.setStatus(Booking.Status.CONFIRMED);

        BookingDto booking2 = new BookingDto();
        booking2.setId(2L);
        booking2.setRoomId(2L);
        booking2.setStatus(Booking.Status.CANCELLED);

        List<BookingDto> bookings = Arrays.asList(booking1, booking2);
        when(bookingService.getUserBookings("testuser")).thenReturn(bookings);

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].roomId").value(1))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].roomId").value(2))
                .andExpect(jsonPath("$[1].status").value("CANCELLED"));

        verify(bookingService).getUserBookings("testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getBookingById_WhenBookingExists_ShouldReturnBooking() throws Exception {
        Long bookingId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(bookingId);
        bookingDto.setRoomId(1L);
        bookingDto.setStatus(Booking.Status.CONFIRMED);

        when(bookingService.getBookingById(bookingId, "testuser")).thenReturn(bookingDto);

        mockMvc.perform(get("/api/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).getBookingById(bookingId, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void cancelBooking_WhenBookingExists_ShouldReturnNoContent() throws Exception {
        Long bookingId = 1L;
        doNothing().when(bookingService).cancelBooking(bookingId, "testuser");

        mockMvc.perform(delete("/api/bookings/{id}", bookingId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookingService).cancelBooking(bookingId, "testuser");
    }

    @Test
    void createBooking_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRoomId(1L);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(3));
        request.setAutoSelect(false);

        mockMvc.perform(post("/api/bookings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(bookingService, never()).createBooking(any(), anyString());
        verify(bookingService, never()).createBookingWithAutoSelect(any(), anyString());
    }
}