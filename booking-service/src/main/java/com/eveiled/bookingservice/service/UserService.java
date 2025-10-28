package com.eveiled.bookingservice.service;

import com.eveiled.bookingservice.dto.AuthResponse;
import com.eveiled.bookingservice.dto.LoginRequest;
import com.eveiled.bookingservice.dto.RegisterRequest;
import com.eveiled.bookingservice.dto.UserDto;
import com.eveiled.bookingservice.entity.User;
import com.eveiled.bookingservice.mapper.BookingMapper;
import com.eveiled.bookingservice.repository.UserRepository;
import com.eveiled.bookingservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void create(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
    }
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        
        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(request.getUsername(), request.getRole().name());
        
        UserDto userDto = bookingMapper.toUserDto(savedUser);
        return new AuthResponse(token, "Bearer", userDto);
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        String token = jwtUtil.generateToken(request.getUsername(), user.getRole().name());
        UserDto userDto = bookingMapper.toUserDto(user);
        
        return new AuthResponse(token, "Bearer", userDto);
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(bookingMapper::toUserDto)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return bookingMapper.toUserDto(user);
    }
    
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setUsername(userDto.getUsername());
        user.setRole(userDto.getRole());
        
        User updatedUser = userRepository.save(user);
        return bookingMapper.toUserDto(updatedUser);
    }
    
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User byUsername = findByUsername(username);

        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                String roleName = byUsername.getRole().name();

                GrantedAuthority authority = () -> roleName;

                return List.of(authority);
            }

            @Override
            public String getPassword() {
                return byUsername.getPassword();
            }

            @Override
            public String getUsername() {
                return byUsername.getUsername();
            }
        };
    }
}
