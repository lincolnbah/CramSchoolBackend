package com.school.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.school.common.enums.MenuTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "選單與按鈕資料")
@TableName("menu")
public class Menu {

    @TableId(type = IdType.AUTO)
    @Schema(description = "uid")
    private Integer id;

    @Schema(description = "選單或按鈕名稱")
    private String name;

    @Schema(description = "功能類型的整數代碼，用於區分 MENU 與 BUTTON，代碼對照尚未定義")
    private MenuTypeEnum type;

    @Schema(description = "前端路由路徑，資料庫預設為空字串")
    private String router;

    @Schema(description = "上層選單 ID")
    private Integer parentId;

    @Schema(description = "建立時間，由資料庫預設填入目前時間")
    private LocalDateTime createTime;

}
