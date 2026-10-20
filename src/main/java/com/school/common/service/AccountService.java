package com.school.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.common.entity.Account;

public interface AccountService extends IService<Account> {

    Account findById(Integer id);

    Account findByAccount(String account);

    boolean updateTokenById(Integer id, String token);

    boolean save(Account account);

}
