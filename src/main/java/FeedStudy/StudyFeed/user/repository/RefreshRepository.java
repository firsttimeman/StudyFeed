package FeedStudy.StudyFeed.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + email, refreshToken, 7, TimeUnit.DAYS);

    }

    public String findByEmail(String email) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + email);
    }

    public void deleteRefreshToken(String email) {
        String key = "refresh_token:" + email;

        System.out.println("🛑 삭제 시도할 키: " + key);
        System.out.println("🛑 삭제 전 값: " + redisTemplate.opsForValue().get(key));

        redisTemplate.delete(key);

        System.out.println("✅ 삭제 후 값: " + redisTemplate.opsForValue().get(key));
    }

    public Long getExpirationTime(String email) {
        return redisTemplate.getExpire(REFRESH_TOKEN_PREFIX + email, TimeUnit.SECONDS);
    }
}
