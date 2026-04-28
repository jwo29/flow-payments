package com.january.flowpayments.user.service;

import com.january.flowpayments.common.exception.CustomException;
import com.january.flowpayments.common.exception.ErrorCode;
import com.january.flowpayments.user.domain.User;
import com.january.flowpayments.user.dto.UserCreateRequestDTO;
import com.january.flowpayments.user.dto.UserResponseDTO;
import com.january.flowpayments.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Long createUser(UserCreateRequestDTO userCreateRequestDTO) {
        userRepository.findByEmail(userCreateRequestDTO.getEmail())
                .ifPresent(user -> {
                    throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
                });

        User user = new User(
                userCreateRequestDTO.getEmail(),
                userCreateRequestDTO.getPassword(),
                userCreateRequestDTO.getName()
        );

        User savedUser = userRepository.save(user);

        return savedUser.getUserId();
    }

    public UserResponseDTO getUser(Long id) {
        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new UserResponseDTO(user);
    }

}
