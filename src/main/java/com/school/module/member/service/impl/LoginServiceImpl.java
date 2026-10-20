package com.school.module.member.service.impl;

import com.school.common.common.api.CommonCode;
import com.school.common.common.constant.RedisKey;
import com.school.common.common.exception.ApiException;
import com.school.common.entity.Account;
import com.school.common.enums.EnableEnum;
import com.school.common.service.AccountService;
import com.school.common.service.JwtService;
import com.school.common.service.RedisService;
import com.school.common.service.SecurityService;
import com.school.common.utils.RSAUtil;
import com.school.module.member.dto.LoginDTO;
import com.school.module.member.service.LoginService;
import com.school.module.member.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final AccountService accountService;

    private final JwtService jwtService;

    private final RedisService redisService;

    private final SecurityService securityService;

    @Override
    public LoginVO login(LoginDTO loginDto) {
        if (!redisService.tryAcquire(String.format(RedisKey.LOGIN_LOCK_KEY, loginDto.getAccount()), 3, TimeUnit.SECONDS)) {
            throw new ApiException(CommonCode.FREQUENT_REQUESTS);
        }

        Account account = accountService.findByAccount(loginDto.getAccount());
        if (account == null) {
            throw new ApiException(CommonCode.ACCOUNT_PASSWORD_NOT_EXIST);
        }

        String bcryptPassword = RSAUtil.decrypt(loginDto.getPassword(), account.getPrivateKey());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (!encoder.matches(bcryptPassword, account.getPassword())) {
            throw new ApiException(CommonCode.ACCOUNT_PASSWORD_NOT_EXIST);
        }

        if (Objects.equals(account.getStatus(), EnableEnum.DISABLE)) {
            throw new ApiException(CommonCode.ACCOUNT_SUSPENDED);
        }

        String token = jwtService.createToken(account.getId(), account.getAccount(), account.getRole().getCode());
        boolean isUpdate = accountService.updateTokenById(account.getId(), token);
        if (Objects.equals(isUpdate, Boolean.FALSE)) {
            throw new ApiException(CommonCode.LOGIN_FAILED);
        }

        LoginVO loginVo = new LoginVO();
        loginVo.setName(account.getName());
        loginVo.setToken(token);

        securityService.setContext(account.getId(), account.getAccount(), account.getRole());

        return loginVo;
    }
}
