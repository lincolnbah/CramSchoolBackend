package com.school.module.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.common.entity.MenuApi;

import java.util.List;
import java.util.Set;

public interface MenuApiService extends IService<MenuApi> {

    List<MenuApi> findAllInMenuId(Set<Integer> menuIdList);

}

