package com.school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.school.entity.Api;
import java.util.List;

public interface ApiService extends IService<Api> {
    /** 查詢角色經由有效選單綁定可使用的指定 HTTP method API。 */
    List<Api> findApis(Integer roleId, Integer method);
}

