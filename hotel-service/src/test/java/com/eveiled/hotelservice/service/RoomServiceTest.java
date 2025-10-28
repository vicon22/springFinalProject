package com.eveiled.hotelservice.service;

import com.eveiled.hotelservice.dto.AvailabilityRequest;
import com.eveiled.hotelservice.dto.HotelDto;
import com.eveiled.hotelservice.dto.RoomDto;
import com.eveiled.hotelservice.entity.Hotel;
import com.eveiled.hotelservice.entity.Room;
import com.eveiled.hotelservice.mapper.HotelMapper;
import com.eveiled.hotelservice.repository.RoomRepository;
import com.eveiled.hotelservice.util.CorrelationIdUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelService hotelService;

    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private RoomService roomService;

    private RoomDto roomDto;
    private Room room;
    private Room savedRoom;
    private HotelDto hotelDto;
    private Hotel hotel;
    private AvailabilityRequest availabilityRequest;

    @BeforeEach
    void setUp() {
        hotelDto = new HotelDto();
        hotelDto.setId(1L);
        hotelDto.setName("Test Hotel");
        hotelDto.setAddress("Test Address");

        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setAddress("Test Address");

        roomDto = new RoomDto();
        roomDto.setNumber("101");
        roomDto.setAvailable(true);
        roomDto.setTimesBooked(0);
        roomDto.setHotelId(1L);

        room = new Room();
        room.setNumber("101");
        room.setAvailable(true);
        room.setTimesBooked(0);
        room.setHotel(hotel);

        savedRoom = new Room();
        savedRoom.setId(1L);
        savedRoom.setNumber("101");
        savedRoom.setAvailable(true);
        savedRoom.setTimesBooked(0);
        savedRoom.setHotel(hotel);

        availabilityRequest = new AvailabilityRequest();
        availabilityRequest.setRequestId("test-request-id");
        availabilityRequest.setStartDate(LocalDateTime.now().plusDays(1));
        availabilityRequest.setEndDate(LocalDateTime.now().plusDays(3));
    }

    @Test
    void createRoom_ShouldReturnCreatedRoom() {
        when(hotelService.getHotelById(1L)).thenReturn(hotelDto);
        when(hotelMapper.toEntity(hotelDto)).thenReturn(hotel);
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);
        when(hotelMapper.toRoomDto(savedRoom)).thenReturn(roomDto);

        RoomDto result = roomService.createRoom(roomDto);

        assertNotNull(result);
        assertEquals(roomDto.getNumber(), result.getNumber());
        
        verify(hotelService).getHotelById(1L);
        verify(hotelMapper).toEntity(hotelDto);
        verify(roomRepository).save(any(Room.class));
        verify(hotelMapper).toRoomDto(savedRoom);
    }

    @Test
    void getAllAvailableRooms_ShouldReturnAvailableRooms() {
        List<Room> rooms = Arrays.asList(savedRoom);
        List<RoomDto> roomDtos = Arrays.asList(roomDto);
        
        when(roomRepository.findByAvailableTrue()).thenReturn(rooms);
        when(hotelMapper.toRoomDtoList(rooms)).thenReturn(roomDtos);

        List<RoomDto> result = roomService.getAllAvailableRooms();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(roomDto.getNumber(), result.get(0).getNumber());
        
        verify(roomRepository).findByAvailableTrue();
        verify(hotelMapper).toRoomDtoList(rooms);
    }

    @Test
    void confirmAvailability_WhenRoomIsAvailable_ShouldReturnTrue() {
        Long roomId = 1L;
        try (MockedStatic<CorrelationIdUtil> mockedStatic = mockStatic(CorrelationIdUtil.class)) {
            mockedStatic.when(CorrelationIdUtil::getCorrelationId).thenReturn("test-correlation-id");
            
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));
            when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

            boolean result = roomService.confirmAvailability(roomId, availabilityRequest);

            assertTrue(result);
            verify(roomRepository).findById(roomId);
            verify(roomRepository).save(savedRoom);
        }
    }

    @Test
    void confirmAvailability_WhenRoomIsNotAvailable_ShouldReturnFalse() {
        Long roomId = 1L;
        savedRoom.setAvailable(false);
        
        try (MockedStatic<CorrelationIdUtil> mockedStatic = mockStatic(CorrelationIdUtil.class)) {
            mockedStatic.when(CorrelationIdUtil::getCorrelationId).thenReturn("test-correlation-id");
            
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));

            boolean result = roomService.confirmAvailability(roomId, availabilityRequest);

            assertFalse(result);
            verify(roomRepository).findById(roomId);
            verify(roomRepository, never()).save(any());
        }
    }

    @Test
    void releaseRoom_WhenRequestIdMatches_ShouldReleaseRoom() {
        Long roomId = 1L;
        String requestId = "test-request-id";
        savedRoom.setBlockedByRequestId(requestId);
        
        try (MockedStatic<CorrelationIdUtil> mockedStatic = mockStatic(CorrelationIdUtil.class)) {
            mockedStatic.when(CorrelationIdUtil::getCorrelationId).thenReturn("test-correlation-id");
            
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));
            when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

            roomService.releaseRoom(roomId, requestId);

            verify(roomRepository).findById(roomId);
            verify(roomRepository).save(savedRoom);
        }
    }
}
