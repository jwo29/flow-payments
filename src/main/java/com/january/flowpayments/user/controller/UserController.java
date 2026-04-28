package com.january.flowpayments.user.controller;

import com.january.flowpayments.common.response.ApiResponse;
import com.january.flowpayments.user.dto.UserCreateRequestDTO;
import com.january.flowpayments.user.dto.UserResponseDTO;
import com.january.flowpayments.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<Long> createUser(@RequestBody UserCreateRequestDTO userCreateRequestDTO) {
        Long userId = userService.createUser(userCreateRequestDTO);
        return ApiResponse.success(userId);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponseDTO> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getUser(id));
    }

}
