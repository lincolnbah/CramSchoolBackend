package com.school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.entity.Account;
import java.util.List;

public interface AccountService extends IService<Account> {
    /** 查詢至多兩筆同帳號資料，讓登入流程可拒絕重複帳號。 */
    List<Account> findLoginAccounts(String account);

    /** 回傳帳號目前且實際存在的角色；帳號或角色不存在則為 null。 */
    Integer findCurrentRole(Integer id);
}

