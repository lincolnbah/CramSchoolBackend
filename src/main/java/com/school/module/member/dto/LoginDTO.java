package com.school.module.member.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {

    @NotBlank(message = "Account value is null")
    private String account;

    @NotBlank(message = "Password value is null")
    private String password;

}
