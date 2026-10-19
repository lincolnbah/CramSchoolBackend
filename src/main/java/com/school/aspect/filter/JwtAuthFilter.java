package com.school.aspect.filter;

import com.school.annotation.TokenVerify;
import com.school.common.api.CommonCode;
import com.school.common.constant.CommonConstants;
import com.school.common.exception.ApiException;
import com.school.dto.LoginUserDTO;
import com.school.enums.RoleEnum;
import com.school.service.AccountService;
import com.school.service.JwtService;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.domain.JWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AccountService accountService;

    private final JwtService jwtService;

    private final RequestMappingHandlerMapping handlerMapping;

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            if (Objects.equals(isAuthNeeded(request), Boolean.FALSE)) {
                chain.doFilter(request, response);
                return;
            }

            if (Objects.equals(isApiNeeded(request), Boolean.FALSE)) {
                chain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.isBlank(authHeader)) {
                log.error("缺少token");
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.LACK_TOKEN));
                return;
            }
            if (!authHeader.startsWith(CommonConstants.RFC_6750_AUTH_SCHEME)) {
                log.error("token格式錯誤");
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.TOKEN_FORMAT_ERROR));
                return;
            }

            JWT decodedToken = jwtService.getDecodedToken(authHeader);
            if (decodedToken.expiration == null) {
                log.error("jwt缺少過期時間");
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.TOKEN_EXPIRED));
                return;
            }

            Map<String, Object> allClaimMap = decodedToken.getAllClaims();
            String account = String.valueOf(allClaimMap.get("account"));
            Integer roleId = Integer.valueOf(String.valueOf(allClaimMap.get("role")));
            RoleEnum roleEnum = RoleEnum.convertRoleEnum(roleId);

            if (StringUtils.isBlank(account) || account.equals("null") || roleEnum == null) {
                log.error("jwt內容錯誤, 無資料, 帳號:{}, role:{}", account, roleId);
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.TOKEN_FALSIFY));
                return;
            }

            if (Objects.equals(accountService.checkAccountStatus(account, roleEnum), Boolean.FALSE)) {
                log.error("帳號權限異常或已被停權, account:{}", account);
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.ACCOUNT_SUSPENDED));
                return;
            }

            if (Objects.equals(jwtService.verifyApiPath(request, roleEnum), Boolean.FALSE)) {
                log.error("帳號:{}, role:{} 無權限", account, roleId);
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.ACCOUNT_NO_PERMISSION));
                return;
            }

            LoginUserDTO loginUserDto = new LoginUserDTO();
            loginUserDto.setAccount(account);
            loginUserDto.setRole(roleEnum);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUserDto,
                            null,
                            Collections.emptyList()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            chain.doFilter(request, response);
        } catch (JWTExpiredException jwtExpiredException) {
            log.error("token過期");
            handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.TOKEN_EXPIRED));
        } catch (Exception e) {
            log.error("token驗證異常, msg:{}", e.getMessage(), e);
            handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.TOKEN_ERROR));
        }
    }

    /**
     * 自定義過濾path
     *
     * @param request request
     * @return boolean
     */
    private boolean isAuthNeeded(HttpServletRequest request) {
        String requestURI = request.getRequestURI().substring(request.getContextPath().length());
        PathPatternParser pathPatternParser = new PathPatternParser();
        PathPattern parse;

        for (String pattern : CommonConstants.SWAGGER_PATTERN_ALLOW_LIST) {
            parse = pathPatternParser.parse(pattern);

            if (parse.matches(PathContainer.parsePath(requestURI))) {
                return Boolean.FALSE;
            }
        }

        for (String pattern : CommonConstants.ADMIN_UI_EXCLUDE_LIST) {
            parse = pathPatternParser.parse(pattern);

            if (parse.matches(PathContainer.parsePath(requestURI))) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 自定義過濾api
     *
     * @param request request
     * @return boolean
     */
    private boolean isApiNeeded(HttpServletRequest request) {
        try {
            HandlerExecutionChain executionChain = handlerMapping.getHandler(request);

            if (executionChain == null || !(executionChain.getHandler() instanceof HandlerMethod)) {
                throw new ApiException(CommonCode.API_HEADER_ERROR);
            }

            HandlerMethod method = (HandlerMethod) executionChain.getHandler();

            TokenVerify tokenVerify = method.getMethodAnnotation(TokenVerify.class);
            if (tokenVerify != null && Objects.equals(tokenVerify.isVerify(), Boolean.FALSE)) {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error("api驗證異常, msg:{}", e.getMessage(), e);
            throw new ApiException(CommonCode.API_HEADER_ERROR);
        }
        return Boolean.TRUE;
    }

    public boolean isPublicRequest(HttpServletRequest request) {
        return !isAuthNeeded(request) || !isApiNeeded(request);
    }

}
