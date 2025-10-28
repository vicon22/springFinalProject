package com.eveiled.hotelservice.service;

import com.eveiled.hotelservice.dto.HotelDto;
import com.eveiled.hotelservice.entity.Hotel;
import com.eveiled.hotelservice.mapper.HotelMapper;
import com.eveiled.hotelservice.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private HotelService hotelService;

    private HotelDto hotelDto;
    private Hotel hotel;
    private Hotel savedHotel;

    @BeforeEach
    void setUp() {
        hotelDto = new HotelDto();
        hotelDto.setName("Test Hotel");
        hotelDto.setAddress("Test Address");

        hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setAddress("Test Address");

        savedHotel = new Hotel();
        savedHotel.setId(1L);
        savedHotel.setName("Test Hotel");
        savedHotel.setAddress("Test Address");
    }

    @Test
    void createHotel_ShouldReturnCreatedHotel() {
        when(hotelMapper.toEntity(hotelDto)).thenReturn(hotel);
        when(hotelRepository.save(hotel)).thenReturn(savedHotel);
        when(hotelMapper.toDto(savedHotel)).thenReturn(hotelDto);

        HotelDto result = hotelService.createHotel(hotelDto);

        assertNotNull(result);
        assertEquals(hotelDto.getName(), result.getName());
        assertEquals(hotelDto.getAddress(), result.getAddress());
        
        verify(hotelMapper).toEntity(hotelDto);
        verify(hotelRepository).save(hotel);
        verify(hotelMapper).toDto(savedHotel);
    }

    @Test
    void getAllHotels_ShouldReturnAllHotels() {
        List<Hotel> hotels = Arrays.asList(savedHotel);
        List<HotelDto> hotelDtos = Arrays.asList(hotelDto);
        
        when(hotelRepository.findAll()).thenReturn(hotels);
        when(hotelMapper.toDtoList(hotels)).thenReturn(hotelDtos);

        List<HotelDto> result = hotelService.getAllHotels();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(hotelDto.getName(), result.get(0).getName());
        
        verify(hotelRepository).findAll();
        verify(hotelMapper).toDtoList(hotels);
    }

    @Test
    void getHotelById_WhenHotelExists_ShouldReturnHotel() {
        Long hotelId = 1L;
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(savedHotel));
        when(hotelMapper.toDto(savedHotel)).thenReturn(hotelDto);

        HotelDto result = hotelService.getHotelById(hotelId);

        assertNotNull(result);
        assertEquals(hotelDto.getName(), result.getName());
        
        verify(hotelRepository).findById(hotelId);
        verify(hotelMapper).toDto(savedHotel);
    }

    @Test
    void getHotelById_WhenHotelNotExists_ShouldThrowException() {
        Long hotelId = 999L;
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> hotelService.getHotelById(hotelId));
        
        assertEquals("Hotel not found with id: 999", exception.getMessage());
        verify(hotelRepository).findById(hotelId);
        verify(hotelMapper, never()).toDto(any());
    }

    @Test
    void updateHotel_WhenHotelExists_ShouldReturnUpdatedHotel() {
        Long hotelId = 1L;
        HotelDto updatedDto = new HotelDto();
        updatedDto.setName("Updated Hotel");
        updatedDto.setAddress("Updated Address");

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(savedHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
        when(hotelMapper.toDto(savedHotel)).thenReturn(updatedDto);

        HotelDto result = hotelService.updateHotel(hotelId, updatedDto);

        assertNotNull(result);
        assertEquals(updatedDto.getName(), result.getName());
        assertEquals(updatedDto.getAddress(), result.getAddress());
        
        verify(hotelRepository).findById(hotelId);
        verify(hotelRepository).save(savedHotel);
        verify(hotelMapper).toDto(savedHotel);
    }
}
