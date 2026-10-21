package com.school.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "選單與 API 關聯")
@TableName("menu_api")
public class MenuApi {

    @TableId(type = IdType.AUTO)
    @Schema(description = "uid")
    private Integer id;

    @Schema(description = "選單 ID，對應 menu.id")
    private Integer menuId;

    @Schema(description = "API ID，對應 api.id")
    private Integer apiId;

}
