package cn.jinynet.site.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.babyfish.jimmer.client.EnableImplicitApi;
import org.babyfish.jimmer.jackson.v2.ImmutableModuleV2;
import org.babyfish.jimmer.sql.EnableDtoGeneration;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * 配置
 *
 * @author jinty
 * @since 1.0
 */
@Configuration
@EnableAsync
@EnableImplicitApi
@EnableDtoGeneration
@EnableFileStorage
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper(ImmutableModuleV2 immutableModuleV2) {
        ObjectMapper objectMapper = new ObjectMapper();

        // 解决类型精度丢失问题
        SimpleModule numberModule = new SimpleModule();
        numberModule.addSerializer(Long.class, ToStringSerializer.instance);
        numberModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        numberModule.addSerializer(BigInteger.class, ToStringSerializer.instance);

        // 定义LocalDateTime序列化/反序列化格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        javaTimeModule.addSerializer(LocalDateTime.class, localDateTimeSerializer);
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

        objectMapper.registerModules(
                immutableModuleV2,
                numberModule,
                new ParameterNamesModule(),
                new Jdk8Module(),
//                new JavaTimeModule()
                javaTimeModule
        );

        // 全局配置
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // 时区处理
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return objectMapper;
    }

    // 定义全局 LocalDateTime 序列化格式
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

//    @Bean
//    @ConditionalOnMissingBean
//    public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        StringRedisSerializer strSerializer = new StringRedisSerializer();
//        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
//
//        RedisTemplate<String, ?> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.afterPropertiesSet();
//
//        redisTemplate.setKeySerializer(strSerializer);
//        redisTemplate.setValueSerializer(jsonRedisSerializer);
//
//        redisTemplate.setHashKeySerializer(strSerializer);
//        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
//
//        return redisTemplate;
//    }
}
