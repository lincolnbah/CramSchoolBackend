package com.school.common.utils;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.dto.LoginUserDTO;
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
            throw new ApiException(CommonCode.TOKEN_EXPIRED);
        }

        return (LoginUserDTO) authentication.getPrincipal();
    }


}
