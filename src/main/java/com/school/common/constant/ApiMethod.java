package com.school.common.constant;

/** api.method 的資料庫代碼，不可依 enum ordinal 推算。 */
public final class ApiMethod {
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;
    public static final int DELETE = 4;
    public static final int PATCH = 5;
    public static final int HEAD = 6;
    public static final int OPTIONS = 7;

    private ApiMethod() { }

    public static Integer fromHttpMethod(String method) {
        if (method == null) return null;
        switch (method) {
            case "GET": return GET;
            case "POST": return POST;
            case "PUT": return PUT;
            case "DELETE": return DELETE;
            case "PATCH": return PATCH;
            case "HEAD": return HEAD;
            case "OPTIONS": return OPTIONS;
            default: return null;
        }
    }
}
