package com.school.module.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.common.entity.Api;

import java.util.List;
import java.util.Set;

public interface ApiService extends IService<Api> {

    List<Api> findAllInId(Set<Integer> idList);

}

