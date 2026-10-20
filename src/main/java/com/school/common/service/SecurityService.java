package com.school.common.service;

import com.school.common.enums.RoleEnum;

public interface SecurityService {

    void setContext(Integer accountId, String account, RoleEnum roleEnum);

}
