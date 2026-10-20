package com.school.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "角色與選單權限關聯")
@TableName("role_menu")
public class RoleMenu {
    @TableId(type = IdType.AUTO)
    @Schema(description = "關聯資料主鍵")
    private Integer id;
    @Schema(description = "角色 ID，對應 role.id")
    private Integer roleId;
    @Schema(description = "選單 ID，對應 menu.id")
    private Integer menuId;
}
