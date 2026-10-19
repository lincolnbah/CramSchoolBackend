package com.school.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.school.common.api.CommonCode;
import com.school.common.constant.CommonConstants;
import com.school.common.exception.ApiException;
import com.school.entity.Api;
import com.school.entity.MenuApi;
import com.school.entity.RoleMenu;
import com.school.enums.RoleEnum;
import com.school.service.ApiService;
import com.school.service.JwtService;
import com.school.service.MenuApiService;
import com.school.service.RoleMenuService;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final RoleMenuService roleMenuService;

    private final MenuApiService menuApiService;

    private final ApiService apiService;

    @Value("${security.auth.jwt-secret}")
    private String hmacSecret;

    @Override
    public JWT getDecodedToken(String tokenWithBearer) {
        JWT decodedToken = new JWT();

        if (tokenWithBearer != null) {
            String encodedJWT = tokenWithBearer.replace(CommonConstants.RFC_6750_AUTH_SCHEME, "");
            Verifier verifier = HMACVerifier.newVerifier(hmacSecret);

            decodedToken = JWT.getDecoder().decode(encodedJWT, verifier);
        }

        return decodedToken;
    }

    @Override
    public boolean verifyApiPath(HttpServletRequest request, RoleEnum roleEnum) {
        if (roleEnum == null) {
            throw new ApiException(CommonCode.PARAM_INVALID);
        }

        List<RoleMenu> roleMenuList = roleMenuService.findAllByRole(roleEnum);
        if (CollUtil.isNotEmpty(roleMenuList)) {
            Set<Integer> menuIdSet = roleMenuList.stream().map(RoleMenu::getMenuId).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(menuIdSet)) {
                List<MenuApi> menuApiList = menuApiService.findAllInMenuId(menuIdSet);
                if (CollUtil.isNotEmpty(menuApiList)) {
                    Set<Integer> apiIdSet = menuApiList.stream().map(MenuApi::getApiId).collect(Collectors.toSet());
                    List<Api> apiList = apiService.findAllInId(apiIdSet);

                    String requestURI = request.getRequestURI().substring(request.getContextPath().length());
                    String method = request.getMethod();
                    for (Api api : apiList) {
                        if (Objects.equals(requestURI, api.getPath()) && api.getMethod() != null &&
                                Objects.equals(method, api.getMethod().name())) {
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

}