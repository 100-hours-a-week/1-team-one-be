package com.raisedeveloper.server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class SchedulerLockConfig {

	@Value("${shedlock.redis.environment}")
	private String shedLockEnvironment;

	@Bean
	public LockProvider lockProvider(RedisConnectionFactory redisConnectionFactory) {
		return new RedisLockProvider(redisConnectionFactory, shedLockEnvironment);
	}
}
