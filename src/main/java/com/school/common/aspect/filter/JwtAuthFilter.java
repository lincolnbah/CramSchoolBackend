package com.school.common.aspect.filter;

import com.school.common.annotation.NotTokenVerify;
import com.school.common.annotation.VerifyPaymentGateway;
import com.school.common.common.api.CommonCode;
import com.school.common.common.constant.CommonConstants;
import com.school.common.common.exception.ApiException;
import com.school.common.config.CustomerActivationStatusConfig;
import com.school.common.dto.LoginUserDTO;
import com.school.common.entity.Account;
import com.school.common.enums.EnableEnum;
import com.school.common.enums.RoleEnum;
import com.school.common.service.AccountService;
import com.school.common.service.JwtService;
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
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
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

    private final CustomerActivationStatusConfig customerActivationStatusConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            if (Objects.equals(this.isAuthNeeded(request), Boolean.FALSE)) {
                chain.doFilter(request, response);
                return;
            }

            HandlerExecutionChain executionChain = this.buildHandlerExecutionChain(request);
            if (Objects.equals(this.isFunctionalStatusNeeded(executionChain), Boolean.FALSE)) {
                log.error("當前功能未開通");
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.FEATURE_NOT_YET_AVAILABLE));
                return;
            }

            if (Objects.equals(this.isApiNeeded(executionChain), Boolean.FALSE)) {
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

            Object accountIdClaim = allClaimMap.get("accountId");
            Integer accountId = null;
            if (accountIdClaim instanceof Integer) {
                accountId = (Integer) accountIdClaim;
            } else if (accountIdClaim instanceof Long) {
                accountId = Math.toIntExact((Long) accountIdClaim);
            } else if (accountIdClaim instanceof BigInteger) {
                accountId = ((BigInteger) accountIdClaim).intValueExact();
            }

            String account = String.valueOf(allClaimMap.get("account"));
            Integer roleId = Integer.valueOf(String.valueOf(allClaimMap.get("role")));
            RoleEnum roleEnum = RoleEnum.convertRoleEnum(roleId);

            if (StringUtils.isBlank(account) || account.equals("null") || roleEnum == null || accountId == null) {
                log.error("jwt內容錯誤, 無資料, 帳號:{}, role:{}, accountId:{}", account, roleEnum, accountId);
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.TOKEN_FALSIFY));
                return;
            }

            Account accountEntity = accountService.findById(accountId);
            if (accountEntity == null) {
                log.error("token 帳號錯誤 無資料, accountId:{}, account:{}", accountId, account);
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.TOKEN_FALSIFY));
                return;
            }

            String token = authHeader.substring(CommonConstants.RFC_6750_AUTH_SCHEME.length());
            if (!Objects.equals(accountEntity.getToken(), token)) {
                log.error("帳號:{}, role:{} 重複登錄", account, roleId);
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.DUPLICATE_LOGIN));
                return;
            }

            if (Objects.equals(accountEntity.getStatus(), EnableEnum.DISABLE)) {
                log.error("帳號權限異常或已被停權, account:{}", account);
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.ACCOUNT_SUSPENDED));
                return;
            }

            if (!Objects.equals(accountEntity.getRole(), roleEnum) ||
                    Objects.equals(jwtService.verifyApiPath(request, roleEnum), Boolean.FALSE)) {
                log.error("帳號:{}, role:{} 無權限", account, roleId);
                handlerExceptionResolver.resolveException(request, response, null, new ApiException(CommonCode.ACCOUNT_NO_PERMISSION));
                return;
            }

            LoginUserDTO loginUserDto = new LoginUserDTO();
            loginUserDto.setAccountId(accountId);
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
     * @param executionChain executionChain
     * @return boolean
     */
    private boolean isApiNeeded(HandlerExecutionChain executionChain) {
        try {
            HandlerMethod method = (HandlerMethod) executionChain.getHandler();

            NotTokenVerify notTokenVerify = method.getMethodAnnotation(NotTokenVerify.class);
            if (notTokenVerify != null) {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error("api驗證異常, msg:{}", e.getMessage(), e);
            throw new ApiException(CommonCode.API_VERIFY_ERROR);
        }
        return Boolean.TRUE;
    }

    /**
     * 檢查功能是否開通
     *
     * @param executionChain executionChain
     * @return boolean
     */
    private boolean isFunctionalStatusNeeded(HandlerExecutionChain executionChain) {
        try {
            HandlerMethod method = (HandlerMethod) executionChain.getHandler();

            VerifyPaymentGateway verifyPaymentGateway = method.getMethodAnnotation(VerifyPaymentGateway.class);
            if (verifyPaymentGateway != null) {
                return Objects.equals(customerActivationStatusConfig.getPaymentGatewayStatus(), 1);
            }
        } catch (Exception e) {
            log.error("api驗證異常, msg:{}", e.getMessage(), e);
            throw new ApiException(CommonCode.API_VERIFY_ERROR);
        }
        return Boolean.TRUE;
    }

    private HandlerExecutionChain buildHandlerExecutionChain(HttpServletRequest request) {
        try {
            if (handlerMapping.usesPathPatterns()
                    && !ServletRequestPathUtils.hasParsedRequestPath(request)) {
                ServletRequestPathUtils.parseAndCache(request);
            }

            HandlerExecutionChain executionChain = handlerMapping.getHandler(request);

            if (executionChain == null || !(executionChain.getHandler() instanceof HandlerMethod)) {
                throw new ApiException(CommonCode.INVALID_REQUEST);
            }
            return executionChain;
        } catch (Exception e) {
            log.error("api驗證異常, msg:{}", e.getMessage(), e);
            throw new ApiException(CommonCode.FAILED);
        }
    }

    /**
     * 免登錄過濾
     *
     * @param request request
     * @return boolean
     */
    public boolean isPublicRequest(HttpServletRequest request) {
        if (!this.isAuthNeeded(request)) {
            return Boolean.TRUE;
        }

        HandlerExecutionChain executionChain = this.buildHandlerExecutionChain(request);
        return !this.isApiNeeded(executionChain);
    }

}
