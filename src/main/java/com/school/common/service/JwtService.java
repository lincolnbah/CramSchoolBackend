package com.school.common.service;

import com.school.common.enums.RoleEnum;
import io.fusionauth.jwt.domain.JWT;

import javax.servlet.http.HttpServletRequest;

public interface JwtService {

    String createToken(Integer accountId, String account, Integer roleId);

    JWT getDecodedToken(String jwtToken);

    boolean verifyApiPath(HttpServletRequest request, RoleEnum roleEnum);
}
