package com.school.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.entity.Account;
import com.school.common.mapper.AccountMapper;
import com.school.common.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public Account findById(Integer id) {
        return super.getById(id);
    }

    @Override
    public Account findByAccount(String account) {
        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Account::getAccount, account);
        return super.getOne(queryWrapper);
    }

    @Override
    public boolean updateTokenById(Integer id, String token) {
        Account account = new Account();
        account.setId(id);
        account.setToken(token);
        return super.updateById(account);
    }

    @Override
    public boolean save(Account account) {
        return super.save(account);
    }
}

