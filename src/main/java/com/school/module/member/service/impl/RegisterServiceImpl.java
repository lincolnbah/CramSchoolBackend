package com.school.module.member.service.impl;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.dto.RSAKeyDTO;
import com.school.common.entity.Account;
import com.school.common.enums.EnableEnum;
import com.school.common.enums.RoleEnum;
import com.school.common.service.AccountService;
import com.school.common.service.JwtService;
import com.school.common.service.SecurityService;
import com.school.common.utils.RSAUtil;
import com.school.module.member.dto.RegisterDTO;
import com.school.module.member.service.RegisterService;
import com.school.module.member.vo.RegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final AccountService accountService;

    private final JwtService jwtService;

    private final SecurityService securityService;

    @Override
    public RegisterVO register(RegisterDTO registerDto) {
        if (!Objects.equals(registerDto.getPassword(), registerDto.getRepeatPassword())) {
            throw new ApiException(CommonCode.INCORRECT_PASSWORD_ENTERED_TWICE);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = encoder.encode(registerDto.getPassword());

        RSAKeyDTO rsaKeyDto = RSAUtil.generateKeyPair();

        Account account = new Account();
        account.setAccount(registerDto.getAccount());
        account.setPassword(password);
        account.setName(registerDto.getName());
        account.setEmail(registerDto.getEmail());
        account.setPhone(registerDto.getPhone());
        account.setStatus(EnableEnum.ENABLE);
        account.setRole(RoleEnum.PARENTS);
        account.setPublicKey(rsaKeyDto.getPublicKey());
        account.setPrivateKey(rsaKeyDto.getPrivateKey());

        boolean isSave = accountService.save(account);
        if (!isSave) {
            throw new ApiException(CommonCode.REGISTRATION_FAILED);
        }

        RegisterVO registerVo = new RegisterVO();

        String token = jwtService.createToken(account.getId(), account.getAccount(), account.getRole().getCode());
        securityService.setContext(account.getId(), account.getAccount(), account.getRole());

        account.setToken(token);
        accountService.updateTokenById(account.getId(), token);

        registerVo.setName(account.getName());
        registerVo.setToken(token);
        return registerVo;
    }
}
