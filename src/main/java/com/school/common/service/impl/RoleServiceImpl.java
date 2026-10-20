package com.school.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.school.common.entity.Role;
import com.school.common.mapper.RoleMapper;
import com.school.module.common.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}

