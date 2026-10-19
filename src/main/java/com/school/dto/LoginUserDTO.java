package com.school.dto;

import com.school.enums.RoleEnum;
import lombok.Data;

@Data
public class LoginUserDTO {

    private String account;

    private RoleEnum role;

}
