package com.amechato.ecommerce_api.backend.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String cellphone;
    private UserType  userType;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
