package com.school.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private static final String LOCK_PREFIX = "redisson:lock:";

    private final RedissonClient redissonClient;

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public boolean tryLock(String key, long time, TimeUnit unit) {
        boolean isLock = Boolean.FALSE;
        try {
            validateLock(key);
            Objects.requireNonNull(unit, "Time unit must not be null");
            isLock = redissonClient.getLock(LOCK_PREFIX + key).tryLock(0, time, unit);
        } catch (InterruptedException e) {
            log.error("redis 獲取鎖失敗, key:{}, time:{}, unit:{}", key, time, unit);
        }
        return isLock;
    }

    @Override
    public boolean unlock(String key) {
        boolean isLock = Boolean.FALSE;
        try {
            validateLock(key);
            redissonClient.getLock(LOCK_PREFIX + key).unlock();
            isLock = Boolean.TRUE;
        } catch (IllegalMonitorStateException e) {
            log.error("redis 獲取鎖失敗, key:{}", key);
        }
        return isLock;
    }

    @Override
    public boolean tryAcquire(String key, long time, TimeUnit unit) { boolean isLock = Boolean.FALSE;
        try {
            isLock = Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent("rate-limit:" + key, "1", time, unit));
        } catch (Exception e) {
            log.error("redis 獲取鎖失敗, key:{}, time:{}, unit:{}", key, time, unit);
        }
        return isLock;
    }


    private void validateLock(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Lock key must not be blank");
        }
    }

    @Override
    public void hashPut(String key, String field, Object value) {
        redisTemplate.<String, Object>opsForHash().put(key, field, value);
    }

    @Override
    public void hashPutAll(String key, Map<String, ?> values) {
        if (!values.isEmpty()) {
            redisTemplate.<String, Object>opsForHash().putAll(key, values);
        }
    }

    @Override
    public <T> T hashGet(Class<T> targetClass, String key, String field) {
        Object value = redisTemplate.<String, Object>opsForHash().get(key, field);
        return value == null ? null : objectMapper.convertValue(value, targetClass);
    }

    @Override
    public Map<String, Object> hashEntries(String key) {
        return redisTemplate.<String, Object>opsForHash().entries(key);
    }

    @Override
    public <T> Map<String, T> hashEntries(Class<T> targetClass, String key) {
        Map<String, T> result = new LinkedHashMap<>();
        hashEntries(key).forEach((field, value) ->
                result.put(field, objectMapper.convertValue(value, targetClass)));
        return result;
    }

    @Override
    public Long hashDelete(String key, String... fields) {
        return fields.length == 0 ? 0L
                : redisTemplate.<String, Object>opsForHash().delete(key, (Object[]) fields);
    }

    @Override
    public boolean hashHasKey(String key, String field) {
        return redisTemplate.<String, Object>opsForHash().hasKey(key, field);
    }

    @Override
    public Long hashIncrement(String key, String field, long number) {
        return redisTemplate.<String, Object>opsForHash().increment(key, field, number);
    }

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setExpireSeconds(String key, Object value, long sec) {
        redisTemplate.opsForValue().set(key, value, sec, TimeUnit.SECONDS);
    }

    @Override
    public void setExpireMilliSeconds(String key, Object value, long milliSeconds) {
        redisTemplate.opsForValue().set(key, value, milliSeconds, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setMinutes(String key, Object value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.MINUTES);
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public <T> T get(Class<T> targetClass, String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : objectMapper.convertValue(value, targetClass);
    }

    @Override
    public Long increment(String key, long number) {
        return redisTemplate.opsForValue().increment(key, number);
    }

    @Override
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }


    @Override
    public String getAndSet(String key, String value) {
        Object previous = redisTemplate.opsForValue().getAndSet(key, value);
        return previous == null ? null : objectMapper.convertValue(previous, String.class);
    }

    @Override
    public Boolean expire(String key, long time, TimeUnit type) {
        return redisTemplate.boundValueOps(key).expire(time, type);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Boolean setIfAbsent(String key, Object value, long time, TimeUnit unit) {
        if (time <= 0) {
            throw new IllegalArgumentException("Expiration must be positive");
        }
        return redisTemplate.opsForValue().setIfAbsent(key, value, time, unit);
    }

}
