package FeedStudy.StudyFeed.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Repository
public class BlackListRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACK_LIST_ACCESS_TOKEN_PREFIX = "blacklist:access_token:";

    private String getKey(String accessToken) {
        return BLACK_LIST_ACCESS_TOKEN_PREFIX + accessToken;
    }

    public void addToBlackList(String accessToken, long expirationTime) {
        redisTemplate.opsForValue().set(
                getKey(accessToken),
                "logout",
                expirationTime,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isTokenBlackListed(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getKey(accessToken)));
    }

}
