package com.school.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.school.common.entity.Menu;
import com.school.common.mapper.MenuMapper;
import com.school.module.common.service.MenuService;
import org.springframework.stereotype.Service;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
}

