package com.school.service;

import com.school.enums.RoleEnum;
import io.fusionauth.jwt.domain.JWT;

import javax.servlet.http.HttpServletRequest;

public interface JwtService {

    JWT getDecodedToken(String jwtToken);

    boolean verifyApiPath(HttpServletRequest request, RoleEnum roleEnum);
}
