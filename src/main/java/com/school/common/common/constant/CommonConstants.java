package com.school.common.common.constant;

import java.util.Arrays;
import java.util.List;

public class CommonConstants {

    public static final String RFC_6750_AUTH_SCHEME = "Bearer ";

    public static final List<String> SWAGGER_PATTERN_ALLOW_LIST = Arrays.asList(
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/favicon.ico"
    );

    public static final List<String> ADMIN_UI_EXCLUDE_LIST = Arrays.asList(
            "/applications",
            "/applications/**",
            "/assets/css/**",
            "/assets/js/**",
            "/sba-settings.js",
            "/assets/img/**",
            "/instances/**",
            "/actuator/**",
            "/actuator"
    );
}
