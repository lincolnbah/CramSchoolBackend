package com.school.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.school.common.enums.MethodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "API 權限設定")
@TableName("api")
public class Api {

    @TableId(type = IdType.AUTO)
    @Schema(description = "uid")
    private Integer id;

    @Schema(description = "API 路徑")
    private String path;

    @Schema(description = "API Method")
    private MethodEnum method;

}
