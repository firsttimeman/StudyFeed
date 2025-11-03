package FeedStudy.StudyFeed.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatCategory {
    SQUAD("chat.squad", "/sub/squad");

    private final String redisChannelPrefix;   // Redis 발행용
    private final String stompDestinationPrefix; // STOMP 구독용

    public String toRedisChannel(Long roomId) {
        return redisChannelPrefix + "." + roomId;
    }

    public String toStompDestination(Long roomId) {
        return stompDestinationPrefix + "/" + roomId;
    }
}