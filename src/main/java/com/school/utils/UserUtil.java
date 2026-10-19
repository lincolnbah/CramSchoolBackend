package com.school.utils;

import com.school.dto.LoginUserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtil {

    private UserUtil() {
    }

    public static LoginUserDTO get() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof LoginUserDTO)) {
            return null;
        }

        return (LoginUserDTO) authentication.getPrincipal();
    }


}
