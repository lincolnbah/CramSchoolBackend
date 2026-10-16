package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "角色資料")
@TableName("role")
public class Role {

    @TableId(type = IdType.AUTO)
    @Schema(description = "角色主鍵")
    private Integer id;

    @Schema(description = "角色名稱")
    private String name;

}
