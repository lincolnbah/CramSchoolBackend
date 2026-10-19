package com.school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.entity.Api;

import java.util.List;
import java.util.Set;

public interface ApiService extends IService<Api> {

    List<Api> findAllInId(Set<Integer> idList);

}

