package com.school.module.member.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RegisterDTO {

    @NotBlank(message = "Account value is null")
    private String account;

    @NotBlank(message = "Password value is null")
    private String password;

    @NotBlank(message = "Repeat Password value is null")
    private String repeatPassword;

    @NotBlank(message = "Name value is null")
    private String name;

    @NotBlank(message = "Email value is null")
    private String email;

    @NotBlank(message = "Phone value is null")
    private String phone;

}
