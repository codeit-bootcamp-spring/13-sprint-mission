package com.sprint.mission.discodeit.config.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            // Spring 이 주입할 빈을 찾을 때, 이름을 통해 검색해서 주입하는 annotation
            @Qualifier("redisSerializer") GenericJackson2JsonRedisSerializer redisSerializer
    ) {
        // Redis 명령 실행을 위한 Spring Data Redis 의 클래스. SDK 와 비슷한 느낌.
        // 커넥션을 얻고, 객체 직렬화, 후처리등 공통 작업 담당
        // redis 키를 String, 값은 여러타입 취급.
        // template.~() method 를 통해 값을 저장, 조회 할 수 있음.
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);   // 연결 획득을 위한 팩토리.

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);

        // 탬플릿 속성 조회 및 serializer 준비 초기화 매서드
        template.afterPropertiesSet();
        return template;
    }

    @Bean("redisSerializer")
    public GenericJackson2JsonRedisSerializer redisSerializer(ObjectMapper objectMapper) {
        ObjectMapper redisObjectMapper = objectMapper.copy();

        /*
        redis 조회 결과에 대한 Dto 타입을 추론하기 위한 데이터.
        간편 설정이기에, 세세한 조절은 찾아봐서 상황에 맞게 설정해야 함.
         */
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,  // 복원 할 객체의 데이터 타입 허용? - 하위타입까지 허용
                ObjectMapper.DefaultTyping.EVERYTHING,  // 어떤 타입에 타입정보? - final 클래스까지 광범위
                JsonTypeInfo.As.PROPERTY    // 타입 정보는 어디에? - json 객체의 속성으로 포함
        );

        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }
}
