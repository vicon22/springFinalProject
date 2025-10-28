package com.eveiled.hotelservice.controller;

import com.eveiled.hotelservice.dto.HotelDto;
import com.eveiled.hotelservice.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HotelController.class)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createHotel_AsAdmin_ShouldReturnCreatedHotel() throws Exception {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setName("Test Hotel");
        hotelDto.setAddress("Test Address");

        HotelDto createdHotel = new HotelDto();
        createdHotel.setId(1L);
        createdHotel.setName("Test Hotel");
        createdHotel.setAddress("Test Address");

        when(hotelService.createHotel(any(HotelDto.class))).thenReturn(createdHotel);

        mockMvc.perform(post("/api/hotels")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hotelDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Hotel"))
                .andExpect(jsonPath("$.address").value("Test Address"));

        verify(hotelService).createHotel(any(HotelDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createHotel_AsUser_ShouldReturnCreatedHotel() throws Exception {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setName("Test Hotel");
        hotelDto.setAddress("Test Address");

        HotelDto createdHotel = new HotelDto();
        createdHotel.setId(1L);
        createdHotel.setName("Test Hotel");
        createdHotel.setAddress("Test Address");

        when(hotelService.createHotel(any(HotelDto.class))).thenReturn(createdHotel);

        mockMvc.perform(post("/api/hotels")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hotelDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Hotel"))
                .andExpect(jsonPath("$.address").value("Test Address"));

        verify(hotelService).createHotel(any(HotelDto.class));
    }

    @Test
    @WithMockUser
    void getAllHotels_ShouldReturnAllHotels() throws Exception {
        HotelDto hotel1 = new HotelDto();
        hotel1.setId(1L);
        hotel1.setName("Hotel 1");
        hotel1.setAddress("Address 1");

        HotelDto hotel2 = new HotelDto();
        hotel2.setId(2L);
        hotel2.setName("Hotel 2");
        hotel2.setAddress("Address 2");

        List<HotelDto> hotels = Arrays.asList(hotel1, hotel2);
        when(hotelService.getAllHotels()).thenReturn(hotels);

        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Hotel 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Hotel 2"));

        verify(hotelService).getAllHotels();
    }

    @Test
    @WithMockUser
    void getHotelById_WhenHotelExists_ShouldReturnHotel() throws Exception {
        Long hotelId = 1L;
        HotelDto hotel = new HotelDto();
        hotel.setId(hotelId);
        hotel.setName("Test Hotel");
        hotel.setAddress("Test Address");

        when(hotelService.getHotelById(hotelId)).thenReturn(hotel);

        mockMvc.perform(get("/api/hotels/{id}", hotelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Hotel"))
                .andExpect(jsonPath("$.address").value("Test Address"));

        verify(hotelService).getHotelById(hotelId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateHotel_AsAdmin_ShouldReturnUpdatedHotel() throws Exception {
        Long hotelId = 1L;
        HotelDto hotelDto = new HotelDto();
        hotelDto.setName("Updated Hotel");
        hotelDto.setAddress("Updated Address");

        HotelDto updatedHotel = new HotelDto();
        updatedHotel.setId(hotelId);
        updatedHotel.setName("Updated Hotel");
        updatedHotel.setAddress("Updated Address");

        when(hotelService.updateHotel(eq(hotelId), any(HotelDto.class))).thenReturn(updatedHotel);

        mockMvc.perform(put("/api/hotels/{id}", hotelId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hotelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Hotel"))
                .andExpect(jsonPath("$.address").value("Updated Address"));

        verify(hotelService).updateHotel(eq(hotelId), any(HotelDto.class));
    }
}