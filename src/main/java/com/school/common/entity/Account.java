package com.school.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.common.enums.EnableEnum;
import com.school.common.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Schema(description = "使用者帳號資料")
@TableName("account")
public class Account {

    @TableId(type = IdType.AUTO)
    @Schema(description = "uid")
    private Integer id;

    @Schema(description = "登入帳號")
    private String account;

    @ToString.Exclude
    @JsonIgnore
    @Schema(description = "登入密碼")
    private String password;

    @Schema(description = "使用者姓名")
    private String name;

    @Schema(description = "角色 ID")
    private RoleEnum role;

    @Schema(description = "聯絡電話")
    private String phone;

    @Schema(description = "電子郵件")
    private String email;

    @Schema(description = "LINE 登入識別碼")
    private String lineId;

    @Schema(description = "Google 登入識別碼")
    private String googleId;

    @Schema(description = "狀態, 1 啟用, 0停用")
    private EnableEnum status;

    @Schema(description = "帳號專屬 RSA 公鑰，SPKI Base64")
    private String publicKey;

    @ToString.Exclude
    @JsonIgnore
    @Schema(description = "經 AES-GCM 加密的 RSA 私鑰，僅限後端使用", hidden = true)
    private String privateKey;

    @ToString.Exclude
    @JsonIgnore
    @Schema(description = "保留的 Token 欄位，目前不參與 JWT 驗證", hidden = true)
    private String token;

    @Schema(description = "更新時間")
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    @Schema(description = "帳號建立時間，由資料庫預設填入目前時間")
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
