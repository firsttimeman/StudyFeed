package FeedStudy.StudyFeed.global.pubsub;

import FeedStudy.StudyFeed.global.dto.ChatBroadcastPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final @Qualifier("clusterNodeId") String clusterNodeId;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatBroadcastPayload payload = objectMapper.readValue(json, ChatBroadcastPayload.class);

            // 자기 자신이 발행한 메시지는 무시 (fan-out loop 방지)
            if (clusterNodeId.equals(payload.getOriginNode())) return;

            String destination = payload.getChatCategory().toStompDestination(payload.getRoomId());
            messagingTemplate.convertAndSend(destination, payload);
            log.info("[Redis Receive] {} -> {}", payload.getChatCategory(), destination);
        } catch (Exception e) {
            log.error("Redis message handling failed", e);
        }
    }
}