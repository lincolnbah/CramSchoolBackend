package com.school.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties properties) {
        if (properties.getCluster() != null || properties.getSentinel() != null
                || StringUtils.hasText(properties.getUrl())) {
            throw new IllegalArgumentException("Redisson currently supports spring.redis standalone host/port settings only");
        }
        Config config = new Config();
        String host = properties.getHost();
        if (host.contains(":") && !host.startsWith("[")) {
            host = "[" + host + "]";
        }
        SingleServerConfig server = config.useSingleServer()
                .setAddress((properties.isSsl() ? "rediss://" : "redis://")
                        + host + ":" + properties.getPort())
                .setDatabase(properties.getDatabase());
        if (StringUtils.hasText(properties.getUsername())) {
            server.setUsername(properties.getUsername());
        }
        if (StringUtils.hasText(properties.getPassword())) {
            server.setPassword(properties.getPassword());
        }
        if (properties.getConnectTimeout() != null) {
            server.setConnectTimeout(Math.toIntExact(properties.getConnectTimeout().toMillis()));
        }
        if (properties.getTimeout() != null) {
            server.setTimeout(Math.toIntExact(properties.getTimeout().toMillis()));
        }
        return Redisson.create(config);
    }
}
