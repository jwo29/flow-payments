package com.january.flowpayments.user.service;

import com.january.flowpayments.user.dto.UserCreateRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    public void createUserTest() {

        UserCreateRequestDTO userCreateRequestDTO = new UserCreateRequestDTO();


        userService.createUser(userCreateRequestDTO);

    }
}
