package com.school.module.member.controller;

import com.school.common.annotation.NotTokenVerify;
import com.school.common.common.api.ApiResult;
import com.school.module.member.dto.LoginDTO;
import com.school.module.member.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("${module-path.member}")
public class LoginController {

    private final LoginService loginService;

    @NotTokenVerify
    @PostMapping("/login")
    public ApiResult<?> login(@Valid @RequestBody LoginDTO loginDto) {
        return ApiResult.success(loginService.login(loginDto));
    }

}
