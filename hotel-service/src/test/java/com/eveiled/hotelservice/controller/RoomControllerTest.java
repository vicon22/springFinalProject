package com.eveiled.hotelservice.controller;

import com.eveiled.hotelservice.dto.AvailabilityRequest;
import com.eveiled.hotelservice.dto.RoomDto;
import com.eveiled.hotelservice.service.RoomService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoom_AsAdmin_ShouldReturnCreatedRoom() throws Exception {
        RoomDto roomDto = new RoomDto();
        roomDto.setNumber("101");
        roomDto.setAvailable(true);
        roomDto.setTimesBooked(0);
        roomDto.setHotelId(1L);

        RoomDto createdRoom = new RoomDto();
        createdRoom.setId(1L);
        createdRoom.setNumber("101");
        createdRoom.setAvailable(true);
        createdRoom.setTimesBooked(0);
        createdRoom.setHotelId(1L);

        when(roomService.createRoom(any(RoomDto.class))).thenReturn(createdRoom);

        mockMvc.perform(post("/api/rooms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("101"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.hotelId").value(1));

        verify(roomService).createRoom(any(RoomDto.class));
    }

    @Test
    @WithMockUser
    void getAllAvailableRooms_ShouldReturnAvailableRooms() throws Exception {
        RoomDto room1 = new RoomDto();
        room1.setId(1L);
        room1.setNumber("101");
        room1.setAvailable(true);
        room1.setTimesBooked(5);
        room1.setHotelId(1L);

        RoomDto room2 = new RoomDto();
        room2.setId(2L);
        room2.setNumber("102");
        room2.setAvailable(true);
        room2.setTimesBooked(3);
        room2.setHotelId(1L);

        List<RoomDto> rooms = Arrays.asList(room1, room2);
        when(roomService.getAllAvailableRooms()).thenReturn(rooms);

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].number").value("101"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].number").value("102"))
                .andExpect(jsonPath("$[1].available").value(true));

        verify(roomService).getAllAvailableRooms();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmAvailability_WhenRoomIsAvailable_ShouldReturnForbidden() throws Exception {
        Long roomId = 1L;
        AvailabilityRequest request = new AvailabilityRequest();
        request.setRequestId("test-request-id");
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(3));

        mockMvc.perform(post("/api/rooms/{id}/confirm-availability", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(roomService, never()).confirmAvailability(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void releaseRoom_ShouldReturnForbidden() throws Exception {
        Long roomId = 1L;
        String requestId = "test-request-id";

        mockMvc.perform(post("/api/rooms/{id}/release", roomId)
                .param("requestId", requestId))
                .andExpect(status().isForbidden());

        verify(roomService, never()).releaseRoom(anyLong(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void incrementTimesBooked_ShouldReturnForbidden() throws Exception {
        Long roomId = 1L;

        mockMvc.perform(post("/api/rooms/{id}/increment-bookings", roomId))
                .andExpect(status().isForbidden());

        verify(roomService, never()).incrementTimesBooked(anyLong());
    }
}