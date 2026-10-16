package com.school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.RoleMenu;
import com.school.mapper.RoleMenuMapper;
import com.school.service.RoleMenuService;
import org.springframework.stereotype.Service;

@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {
}

