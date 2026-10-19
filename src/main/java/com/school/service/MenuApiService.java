package com.school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.entity.MenuApi;

import java.util.List;
import java.util.Set;

public interface MenuApiService extends IService<MenuApi> {

    List<MenuApi> findAllInMenuId(Set<Integer> menuIdList);

}

