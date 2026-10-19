package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Account;
import com.school.enums.EnableEnum;
import com.school.enums.RoleEnum;
import com.school.mapper.AccountMapper;
import com.school.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public boolean checkAccountStatus(String account, RoleEnum role) {
        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Account::getAccount, account.trim());
        Account accountEntity = super.getOne(queryWrapper);

        if (accountEntity == null) {
            log.error("帳號錯誤, account:{}", account);
            return Boolean.FALSE;
        }

        if (Objects.equals(accountEntity.getStatus(), EnableEnum.DISABLE)) {
            log.error("帳號已停權, account:{}", account);
            return Boolean.FALSE;
        }

        if (!Objects.equals(accountEntity.getRole(), role)) {
            log.error("帳號腳色錯誤, account:{}, role:{}", account, role);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }
}

