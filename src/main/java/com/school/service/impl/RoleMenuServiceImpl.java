package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.RoleMenu;
import com.school.enums.RoleEnum;
import com.school.mapper.RoleMenuMapper;
import com.school.service.RoleMenuService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {

    @Override
    public List<RoleMenu> findAllByRole(RoleEnum roleEnum) {
        LambdaQueryWrapper<RoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoleMenu::getRoleId, roleEnum);
        return super.list(queryWrapper);
    }
}

