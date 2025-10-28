package com.eveiled.bookingservice.mapper;

import com.eveiled.bookingservice.dto.BookingDto;
import com.eveiled.bookingservice.dto.UserDto;
import com.eveiled.bookingservice.entity.Booking;
import com.eveiled.bookingservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    
    @Mapping(target = "userId", source = "user.id")
    BookingDto toDto(Booking booking);
    
    List<BookingDto> toDtoList(List<Booking> bookings);
    
    @Mapping(target = "id", ignore = true)
    UserDto toUserDto(User user);
}
