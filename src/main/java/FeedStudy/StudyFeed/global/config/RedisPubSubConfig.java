package FeedStudy.StudyFeed.global.config;

import FeedStudy.StudyFeed.global.pubsub.RedisChatSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private static final String SQUAD_CHAT_CHANNEL = "chat.squad.*";

    @Bean(name = "clusterNodeId")
    public String clusterNodeId() {
        return UUID.randomUUID().toString();
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory,
            RedisChatSubscriber subscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        // 패턴 구독으로 등록
        container.addMessageListener(subscriber, new PatternTopic(SQUAD_CHAT_CHANNEL));
        return container;
    }
}