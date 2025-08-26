package com.example.redisdemo_1.mq;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RedisStreamUtils {
    private final StringRedisTemplate stringRedisTemplate;

    private final StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    public RedisStreamUtils(RedisConnectionFactory connectionFactory) {
        this.stringRedisTemplate = new StringRedisTemplate();
        this.stringRedisTemplate.setConnectionFactory(connectionFactory);
        this.stringRedisTemplate.setKeySerializer(stringRedisSerializer);
        this.stringRedisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        this.stringRedisTemplate.afterPropertiesSet();
    }

    /**
     * 创建消费者组
     *
     * @param streamKey
     * @param groupName
     */
    public void createConsumerGroup(String streamKey, String groupName) {
        stringRedisTemplate.execute((RedisConnection connection) -> {
            RedisStreamCommands streamCommands = connection.streamCommands();
            try {
                //检查消费组是否存在
                streamCommands.xInfoConsumers(stringRedisSerializer.serialize(streamKey), groupName);
            } catch (Exception e) {
                //如果获取失败，创建消费组
                streamCommands.xGroupCreate(
                        stringRedisSerializer.serialize(streamKey),
                        groupName,
                        ReadOffset.latest()
                );
            }
            return null;
        });
    }

    /**
     * 发送消息到stream
     */
    public void sendMessage(String streamKey, String message) {
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("data", message);
        stringRedisTemplate.opsForStream().add(streamKey, messageMap);
    }

    /**
     * 消费消息
     *
     * @param streamKey
     * @param groupName
     * @param consumerName
     * @return
     */
    public List<MapRecord<String, Object, Object>> consumeMessage(String streamKey, String groupName, String consumerName) {
        return stringRedisTemplate.opsForStream().read(
                Consumer.from(groupName, consumerName),
                StreamReadOptions.empty().count(10).block(Duration.ofSeconds(1))
        );
    }

    /**
     * 确认消息已经被消费
     */
    public void acknowledgeMessage(String streamKey, String groupName, String messageId) {
        stringRedisTemplate.opsForStream().acknowledge(streamKey, groupName, messageId);
    }
}
