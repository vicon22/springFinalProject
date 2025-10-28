package com.eveiled.hotelservice.service;

import com.eveiled.hotelservice.dto.HotelDto;
import com.eveiled.hotelservice.entity.Hotel;
import com.eveiled.hotelservice.mapper.HotelMapper;
import com.eveiled.hotelservice.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelService {
    
    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    
    public HotelDto createHotel(HotelDto hotelDto) {
        Hotel hotel = hotelMapper.toEntity(hotelDto);
        Hotel savedHotel = hotelRepository.save(hotel);
        return hotelMapper.toDto(savedHotel);
    }
    
    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();
        return hotelMapper.toDtoList(hotels);
    }
    
    @Transactional(readOnly = true)
    public HotelDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
        return hotelMapper.toDto(hotel);
    }
    
    public HotelDto updateHotel(Long id, HotelDto hotelDto) {
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
        
        existingHotel.setName(hotelDto.getName());
        existingHotel.setAddress(hotelDto.getAddress());
        
        Hotel updatedHotel = hotelRepository.save(existingHotel);
        return hotelMapper.toDto(updatedHotel);
    }
    
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new RuntimeException("Hotel not found with id: " + id);
        }
        hotelRepository.deleteById(id);
    }
}
