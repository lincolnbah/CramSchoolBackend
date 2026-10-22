package com.school.module.order.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class OrderUtil {

    /**
     * 建立訂單號
     */
    public static String buildOrderNo() {
        Random rand = new Random();
        String date = new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
        return String.format("TW%s", date + rand.nextInt(10));
    }

}
