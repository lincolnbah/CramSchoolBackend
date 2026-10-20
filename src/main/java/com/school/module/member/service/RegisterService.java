package com.school.module.member.service;

import com.school.module.member.dto.RegisterDTO;
import com.school.module.member.vo.RegisterVO;

public interface RegisterService {

    RegisterVO register(RegisterDTO registerDto);

}
