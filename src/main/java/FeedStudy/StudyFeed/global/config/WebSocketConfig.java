package FeedStudy.StudyFeed.global.config;

import jakarta.persistence.Entity;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // stomp msg broker enable
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocketMessageBrokerConfigurer 커스텀 기능

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // 특정 메세지를 구독하고 싶으면 /topic으로 구독을 한다
                                                                // /topic/chatroom/1
        registry.setApplicationDestinationPrefixes("/app"); // /app/hello
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }
}
