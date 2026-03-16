package com.january.ledgerflow.user.service;

import com.january.ledgerflow.user.domain.User;
import com.january.ledgerflow.user.dto.UserCreateRequestDTO;
import com.january.ledgerflow.user.dto.UserResponseDTO;
import com.january.ledgerflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Long createUser(UserCreateRequestDTO userCreateRequestDTO) {
        userRepository.findByEmail(userCreateRequestDTO.getEmail())
                .ifPresent(user -> {
                    throw new IllegalStateException("User already exists!");
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
                .orElseThrow(() -> new IllegalStateException("User not found!"));

        return new UserResponseDTO(user);
    }

}
