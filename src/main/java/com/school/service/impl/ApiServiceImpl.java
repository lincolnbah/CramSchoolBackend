package com.school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Api;
import com.school.entity.Menu;
import com.school.entity.MenuApi;
import com.school.entity.RoleMenu;
import com.school.mapper.ApiMapper;
import com.school.service.ApiService;
import com.school.service.RoleService;
import com.school.service.RoleMenuService;
import com.school.service.MenuService;
import com.school.service.MenuApiService;
import lombok.RequiredArgsConstructor;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiServiceImpl extends ServiceImpl<ApiMapper, Api> implements ApiService {
    private final RoleService roles;
    private final RoleMenuService roleMenus;
    private final MenuService menus;
    private final MenuApiService menuApis;

    @Override
    public List<Api> findApis(Integer roleId, Integer method) {
        if (roleId == null || method == null || roles.getById(roleId) == null) return Collections.emptyList();

        List<Integer> menuIds = roleMenus.lambdaQuery().select(RoleMenu::getMenuId)
                .eq(RoleMenu::getRoleId, roleId).list().stream().map(RoleMenu::getMenuId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (menuIds.isEmpty()) return Collections.emptyList();

        // 保留原 INNER JOIN 語意：已刪除或不存在的選單不能授予 API 權限。
        List<Integer> existingMenuIds = menus.lambdaQuery().select(Menu::getId)
                .in(Menu::getId, menuIds).list().stream().map(Menu::getId).collect(Collectors.toList());
        if (existingMenuIds.isEmpty()) return Collections.emptyList();

        List<Integer> apiIds = menuApis.lambdaQuery().select(MenuApi::getApiId)
                .in(MenuApi::getMenuId, existingMenuIds).list().stream().map(MenuApi::getApiId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (apiIds.isEmpty()) return Collections.emptyList();

        return lambdaQuery().select(Api::getId, Api::getPath, Api::getMethod)
                .in(Api::getId, apiIds).eq(Api::getMethod, method).list();
    }
}

