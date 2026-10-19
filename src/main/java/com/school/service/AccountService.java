package com.school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.entity.Account;
import com.school.enums.RoleEnum;

public interface AccountService extends IService<Account> {

    boolean checkAccountStatus(String account, RoleEnum roleEnum);
}

