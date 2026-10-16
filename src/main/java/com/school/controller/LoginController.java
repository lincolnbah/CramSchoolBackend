package com.school.controller;

import com.school.common.api.ApiResult;
import com.school.dto.LoginDTO;
import com.school.vo.LoginVO;
import com.school.dto.PublicKeyDTO;
import com.school.dto.RegisterDTO;
import com.school.vo.PublicKeyVO;
import com.school.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {
    private final AuthService service;

    @PostMapping("/public-key")
    public ApiResult<PublicKeyVO> publicKey(@Valid @RequestBody PublicKeyDTO request) {
        return ApiResult.success(service.publicKey(request));
    }

    @PostMapping("/register")
    public ApiResult<Void> register(@Valid @RequestBody RegisterDTO request) {
        service.register(request);
        return ApiResult.success();
    }

    @PostMapping("/login")
    public ApiResult<LoginVO> login(@Valid @RequestBody LoginDTO request) {
        return ApiResult.success(service.login(request));
    }
}
