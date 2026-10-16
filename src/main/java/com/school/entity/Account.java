package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.ToString;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Data
@Schema(description = "使用者帳號資料")
@TableName("account")
public class Account {

    @TableId(type = IdType.AUTO)
    @Schema(description = "帳號主鍵")
    private Integer id;

    @Schema(description = "登入帳號")
    private String account;

    @ToString.Exclude
    @JsonIgnore
    @Schema(description = "登入密碼")
    private String password;

    @Schema(description = "使用者姓名")
    private String name;

    @Schema(description = "角色 ID，對應 role.id，資料庫預設為 4（家長）")
    private Integer role;

    @Schema(description = "聯絡電話")
    private String phone;

    @Schema(description = "電子郵件")
    private String email;

    @Schema(description = "LINE 登入識別碼")
    private String lineId;

    @Schema(description = "Google 登入識別碼")
    private String googleId;

    @Schema(description = "帳號專屬 RSA 公鑰，SPKI Base64")
    private String publicKey;

    @ToString.Exclude
    @JsonIgnore
    @TableField(select = false)
    @Schema(description = "經 AES-GCM 加密的 RSA 私鑰，僅限後端使用", hidden = true)
    private String privateKey;

    @ToString.Exclude
    @JsonIgnore
    @TableField(select = false)
    @Schema(description = "保留的 Token 欄位，目前不參與 JWT 驗證", hidden = true)
    private String token;

    @Schema(description = "帳號建立時間，由資料庫預設填入目前時間")
    private LocalDateTime createTime;
}
