package com.school.service;

import com.school.dto.LoginDTO;
import com.school.vo.LoginVO;
import com.school.dto.PublicKeyDTO;
import com.school.dto.RegisterDTO;
import com.school.vo.PublicKeyVO;

public interface AuthService {

    LoginVO login(LoginDTO request);

    PublicKeyVO publicKey(PublicKeyDTO request);

    void register(RegisterDTO request);

}
