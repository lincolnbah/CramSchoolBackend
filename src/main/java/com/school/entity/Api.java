package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "API 權限設定")
@TableName("api")
public class Api {

    @TableId(type = IdType.AUTO)
    @Schema(description = "API 主鍵")
    private Integer id;

    @Schema(description = "API 路徑")
    private String path;

    @Schema(description = "HTTP 方法：1 GET、2 POST、3 PUT、4 DELETE、5 PATCH、6 HEAD、7 OPTIONS")
    private Integer method;

}
