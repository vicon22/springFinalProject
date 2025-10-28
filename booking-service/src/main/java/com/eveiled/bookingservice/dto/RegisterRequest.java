package com.eveiled.bookingservice.dto;

import com.eveiled.bookingservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private User.Role role = User.Role.USER;
}
