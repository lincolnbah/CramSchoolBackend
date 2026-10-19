package com.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Api;
import com.school.mapper.ApiMapper;
import com.school.service.ApiService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ApiServiceImpl extends ServiceImpl<ApiMapper, Api> implements ApiService {

    @Override
    public List<Api> findAllInId(Set<Integer> idList) {
        LambdaQueryWrapper<Api> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Api::getId, idList);
        return super.list(queryWrapper);
    }
}

