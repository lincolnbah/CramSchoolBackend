package com.school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.entity.Account;
import com.school.mapper.AccountMapper;
import com.school.service.AccountService;
import com.school.service.RoleService;
import lombok.RequiredArgsConstructor;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    private final RoleService roles;

    @Override
    public List<Account> findLoginAccounts(String account) {
        if (account == null) return Collections.emptyList();
        return page(new Page<Account>(1, 2, false), Wrappers.<Account>lambdaQuery()
                .select(Account::getId, Account::getAccount, Account::getPassword, Account::getRole,
                        Account::getPublicKey, Account::getPrivateKey)
                .eq(Account::getAccount, account)).getRecords();
    }

    @Override
    public Integer findCurrentRole(Integer id) {
        if (id == null) return null;
        Account account = lambdaQuery().select(Account::getId, Account::getRole)
                .eq(Account::getId, id).one();
        if (account == null || account.getRole() == null || roles.getById(account.getRole()) == null) return null;
        return account.getRole();
    }
}

