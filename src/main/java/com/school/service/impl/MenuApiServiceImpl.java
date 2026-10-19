package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.MenuApi;
import com.school.mapper.MenuApiMapper;
import com.school.service.MenuApiService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MenuApiServiceImpl extends ServiceImpl<MenuApiMapper, MenuApi> implements MenuApiService {

    @Override
    public List<MenuApi> findAllInMenuId(Set<Integer> menuIdList) {
        LambdaQueryWrapper<MenuApi> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MenuApi::getMenuId, menuIdList);
        return super.list(queryWrapper);
    }

}

