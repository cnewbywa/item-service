package com.cnewbywa.item.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisCacheConfig {

	@Value("${spring.cache.redis.time-to-live}")
    private long ttl;
	
	@Bean
    RedisCacheConfiguration defaultCacheConfig(ObjectMapper objectMapper) {
		ObjectMapper newObjectMapper = new ObjectMapper();
		newObjectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
		newObjectMapper.registerModule(new JavaTimeModule());
		
        return RedisCacheConfiguration.defaultCacheConfig()
        		.entryTtl(Duration.ofMillis(ttl))
        		.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(newObjectMapper)));
    }
	
}
