package com.school.module.member.service;

import com.school.module.member.dto.LoginDTO;
import com.school.module.member.vo.LoginVO;

public interface LoginService {

    LoginVO login(LoginDTO loginDto);

}
