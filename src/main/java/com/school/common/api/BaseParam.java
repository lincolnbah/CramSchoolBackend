package com.school.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseParam implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 每頁顯示筆數，預設為 10
     */
    protected Long size = 10L;

    /**
     * 目前頁碼
     */
    protected Long current = 1L;
}
