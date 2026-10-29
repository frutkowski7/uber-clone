package com.uber_clone.Request;

import com.uber_clone.model.User.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {

    private String email;
    private String password;
    private Role role;

}
