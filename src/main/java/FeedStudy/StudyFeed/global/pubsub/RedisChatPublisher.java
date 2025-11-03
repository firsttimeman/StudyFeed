package FeedStudy.StudyFeed.global.pubsub;

import FeedStudy.StudyFeed.global.dto.ChatBroadcastPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String clusterNodeId;

    public void publish(ChatBroadcastPayload payload) {
        payload.setOriginNode(clusterNodeId);

        try {
            String channel = payload.getChatCategory().toRedisChannel(payload.getRoomId());
            String json = objectMapper.writeValueAsString(payload);

            redisTemplate.convertAndSend(channel, json);
            log.info("[Redis Publish] channel={}, payload={}", channel, payload);

        } catch (Exception e) {
            log.error("Redis publish failed", e);
            throw new RuntimeException("Redis publish failed", e);
        }
    }
}