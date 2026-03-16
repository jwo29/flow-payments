package com.january.ledgerflow.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class UserCreateRequestDTO {

    private String email;
    private String name;
    private String password;

}
