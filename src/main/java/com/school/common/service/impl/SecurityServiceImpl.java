package com.school.common.service.impl;

import com.school.common.dto.LoginUserDTO;
import com.school.common.enums.RoleEnum;
import com.school.common.service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class SecurityServiceImpl implements SecurityService {

    @Override
    public void setContext(Integer accountId, String account, RoleEnum roleEnum) {
        LoginUserDTO user = new LoginUserDTO();
        user.setAccountId(accountId);
        user.setAccount(account);
        user.setRole(roleEnum);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user, null, Collections.emptyList());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
