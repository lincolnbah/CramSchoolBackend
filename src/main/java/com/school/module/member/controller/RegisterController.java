package com.school.module.member.controller;

import com.school.common.annotation.NotTokenVerify;
import com.school.common.common.api.ApiResult;
import com.school.module.member.dto.LoginDTO;
import com.school.module.member.dto.RegisterDTO;
import com.school.module.member.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("${module-path.member}")
public class RegisterController {

    private final RegisterService registerService;

    @NotTokenVerify
    @PostMapping("/register")
    public ApiResult<?> register(@Valid @RequestBody RegisterDTO registerDto) {
        return ApiResult.success(registerService.register(registerDto));
    }

}
