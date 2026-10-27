package com.school.common.common.constant;

public interface RedisKey {

    String LOGIN_LOCK_KEY = "login:lock:%s";

    String PAYMENT_NOTIFY_LOCK = "payment:notify:lock:";
    Long PAYMENT_NOTIFY_TIME = 10L;


}
