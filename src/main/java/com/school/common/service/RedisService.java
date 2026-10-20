package com.school.common.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface RedisService {

    boolean tryLock(String key, long time, TimeUnit unit);

    boolean unlock(String key);

    boolean tryAcquire(String key, long time, TimeUnit unit);

    void set(String key, Object value);

    void setExpireSeconds(String key, Object value, long sec);

    void setExpireMilliSeconds(String key, Object value, long milliSeconds);

    void setMinutes(String key, Object value, long time);

    Long getExpire(String key);

    <T> T get(Class<T> targetClass, String key);

    Long increment(String key, long number);

    Boolean setIfAbsent(String key, Object value);

    Boolean setIfAbsent(String key, Object value, long time, TimeUnit unit);

    String getAndSet(String key, String value);

    Boolean expire(String key, long time, TimeUnit type);

    void delete(String key);

    boolean hasKey(String key);

    void hashPut(String key, String field, Object value);

    void hashPutAll(String key, Map<String, ?> values);

    <T> T hashGet(Class<T> targetClass, String key, String field);

    Map<String, Object> hashEntries(String key);

    <T> Map<String, T> hashEntries(Class<T> targetClass, String key);

    Long hashDelete(String key, String... fields);

    boolean hashHasKey(String key, String field);

    Long hashIncrement(String key, String field, long number);

}
