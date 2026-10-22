package com.school.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum RoleEnum {

    ADMIN(1, "管理員"),
    CRAM_SCHOOL(2, "補習班"),
    TUTOR(3, "補習班老師"),
    PARENTS(4, "家長"),

    ;

    @EnumValue
    private final Integer code;

    private final String text;

    RoleEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public static RoleEnum convertRoleEnum(Integer roleId) {
        if (roleId == null) {
            return null;
        }

        for (RoleEnum roleEnum : RoleEnum.values()) {
            if (roleEnum.getCode().equals(roleId)) {
                return roleEnum;
            }
        }
        return null;
    }
}
