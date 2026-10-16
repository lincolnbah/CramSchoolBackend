package com.school.security;

import com.school.common.constant.ApiMethod;
import com.school.entity.Api;
import com.school.service.AccountService;
import com.school.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ApiAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final AccountService accounts;
    private final ApiService permissions;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (!(auth instanceof JwtAuthenticationToken) || !auth.isAuthenticated()) return deny();
        JwtAuthenticationToken jwt = (JwtAuthenticationToken) auth;
        Integer id;
        Integer role;
        try {
            id = Integer.valueOf(jwt.getToken().getSubject());
            role = Integer.valueOf(jwt.getToken().getClaim("role").toString());
        } catch (RuntimeException exception) { return deny(); }
        if (!role.equals(accounts.findCurrentRole(id))) return deny();
        Integer method = ApiMethod.fromHttpMethod(context.getRequest().getMethod());
        if (method == null) return deny();
        for (Api api : permissions.findApis(role, method)) {
            if (api.getPath() != null && api.getPath().startsWith("/")
                    && new AntPathRequestMatcher(api.getPath(), context.getRequest().getMethod())
                    .matches(context.getRequest())) return new AuthorizationDecision(true);
        }
        return deny();
    }

    private AuthorizationDecision deny() { return new AuthorizationDecision(false); }
}
