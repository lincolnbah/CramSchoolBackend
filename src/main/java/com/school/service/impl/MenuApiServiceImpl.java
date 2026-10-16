package com.school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.MenuApi;
import com.school.mapper.MenuApiMapper;
import com.school.service.MenuApiService;
import org.springframework.stereotype.Service;

@Service
public class MenuApiServiceImpl extends ServiceImpl<MenuApiMapper, MenuApi> implements MenuApiService {
}

