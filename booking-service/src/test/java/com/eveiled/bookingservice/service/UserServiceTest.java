package com.eveiled.bookingservice.service;

import com.eveiled.bookingservice.dto.AuthResponse;
import com.eveiled.bookingservice.dto.LoginRequest;
import com.eveiled.bookingservice.dto.RegisterRequest;
import com.eveiled.bookingservice.dto.UserDto;
import com.eveiled.bookingservice.entity.User;
import com.eveiled.bookingservice.mapper.BookingMapper;
import com.eveiled.bookingservice.repository.UserRepository;
import com.eveiled.bookingservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(User.Role.USER);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setRole(User.Role.USER);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setRole(User.Role.USER);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        authResponse = new AuthResponse();
        authResponse.setToken("jwt-token");
        authResponse.setType("Bearer");
        authResponse.setUser(userDto);
    }

    @Test
    void register_WhenUsernameNotExists_ShouldReturnAuthResponse() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken("testuser", "USER")).thenReturn("jwt-token");
        when(bookingMapper.toUserDto(user)).thenReturn(userDto);

        AuthResponse result = userService.register(registerRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("Bearer", result.getType());
        assertEquals(userDto.getUsername(), result.getUser().getUsername());

        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("testuser", "USER");
        verify(bookingMapper).toUserDto(user);
    }

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnAuthResponse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "USER")).thenReturn("jwt-token");
        when(bookingMapper.toUserDto(user)).thenReturn(userDto);

        AuthResponse result = userService.login(loginRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("Bearer", result.getType());
        assertEquals(userDto.getUsername(), result.getUser().getUsername());

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password", "encodedPassword");
        verify(jwtUtil).generateToken("testuser", "USER");
        verify(bookingMapper).toUserDto(user);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> users = List.of(user);
        List<UserDto> userDtos = List.of(userDto);
        
        when(userRepository.findAll()).thenReturn(users);
        when(bookingMapper.toUserDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDto.getUsername(), result.get(0).getUsername());

        verify(userRepository).findAll();
        verify(bookingMapper).toUserDto(user);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userDto.getUsername(), result.getUsername());

        verify(userRepository).findById(userId);
        verify(bookingMapper).toUserDto(user);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("USER", authorities.iterator().next().getAuthority());

        verify(userRepository).findByUsername("testuser");
    }
}