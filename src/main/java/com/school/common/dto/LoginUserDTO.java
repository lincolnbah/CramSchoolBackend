package com.school.common.dto;

import com.school.common.enums.RoleEnum;
import lombok.Data;

@Data
public class LoginUserDTO {

    private Integer accountId;

    private String account;

    private RoleEnum role;

}
