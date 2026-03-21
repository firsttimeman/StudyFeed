package FeedStudy.StudyFeed.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_TTL_DAYS = 7;



    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + email,
                refreshToken,
                REFRESH_TOKEN_TTL_DAYS,
                TimeUnit.DAYS);

    }

    public String findByEmail(String email) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + email);
    }

    public void deleteRefreshToken(String email) {
        String key = "refresh_token:" + email;
        redisTemplate.delete(key);
    }

    public Long getExpirationTime(String email) {
        return redisTemplate.getExpire(REFRESH_TOKEN_PREFIX + email, TimeUnit.SECONDS);
    }
}
