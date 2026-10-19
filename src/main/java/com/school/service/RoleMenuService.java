package com.school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.entity.RoleMenu;
import com.school.enums.RoleEnum;

import java.util.List;

public interface RoleMenuService extends IService<RoleMenu> {

    List<RoleMenu> findAllByRole(RoleEnum roleEnum);

}

