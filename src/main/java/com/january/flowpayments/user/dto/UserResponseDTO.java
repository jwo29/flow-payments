package com.january.flowpayments.user.dto;

import com.january.flowpayments.user.domain.User;
import lombok.Getter;

@Getter
public class UserResponseDTO {

    private Long userId;
    private String email;
    private String name;

    public UserResponseDTO(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.name = user.getName();
    }
}
