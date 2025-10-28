package com.eveiled.hotelservice.mapper;

import com.eveiled.hotelservice.dto.HotelDto;
import com.eveiled.hotelservice.dto.RoomDto;
import com.eveiled.hotelservice.entity.Hotel;
import com.eveiled.hotelservice.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HotelMapper {
    
    HotelDto toDto(Hotel hotel);
    
    Hotel toEntity(HotelDto hotelDto);
    
    List<HotelDto> toDtoList(List<Hotel> hotels);
    
    @Mapping(target = "hotelId", source = "hotel.id")
    RoomDto toRoomDto(Room room);
    
    List<RoomDto> toRoomDtoList(List<Room> rooms);
}
