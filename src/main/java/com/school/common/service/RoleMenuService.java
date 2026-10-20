package com.school.module.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.common.entity.RoleMenu;
import com.school.common.enums.RoleEnum;


import java.util.List;

public interface RoleMenuService extends IService<RoleMenu> {

    List<RoleMenu> findAllByRole(RoleEnum roleEnum);

}

