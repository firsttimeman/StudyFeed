package FeedStudy.StudyFeed.auth.service;

import FeedStudy.StudyFeed.global.exception.exceptiontype.AuthCodeException;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthCodeService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final long AUTH_CODE_TTL_MINUTES = 10;
    private static final long VERIFIED_TTL_MINUTES = 30;

    private static final String AUTH_CODE_PREFIX = "auth:code:";
    private static final String VERIFIED_PREFIX = "auth:verified:";


    public void saveAuthCode(String email, String authCode) {
        redisTemplate.opsForValue().set(
                 AUTH_CODE_PREFIX + email,
                authCode,
                AUTH_CODE_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        log.info("이메일 인증코드 저장 완료. email={}", email);
    }

    public boolean checkAuthCode(String email, String authCode) {
        String storedCode = redisTemplate.opsForValue().get(AUTH_CODE_PREFIX + email);


        log.info("storedCode=[{}]", storedCode);
        log.info("authCode=[{}]", authCode);

        if(storedCode == null) {
          return false;
        }

        if(!storedCode.equals(authCode)) {
            return false;
        }

        redisTemplate.delete(AUTH_CODE_PREFIX + email);

        redisTemplate.opsForValue().set(
            VERIFIED_PREFIX + email,
                "true",
                VERIFIED_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        log.info("이메일 인증 성공. email={}", email);
        return true;
    }

    public boolean isVerified(String email) {
        String value = redisTemplate.opsForValue().get(VERIFIED_PREFIX + email);
        return "true".equals(value);
    }

    public void removeVerified(String email) {
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }



    public String generateAuthCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < 8; i++) {
            int index = random.nextInt(3);
            switch(index) {
                case 0 -> sb.append((char) (random.nextInt(26) + 'a'));
                case 1 -> sb.append((char) (random.nextInt(26) + 'A'));
                case 2 -> sb.append(random.nextInt(10));
            }
        }
        return sb.toString();
    }

    public void validateVerified(String email) {
        if(!isVerified(email)) {
            throw new AuthCodeException(ErrorCode.AUTH_CODE_MISMATCH);
        }
    }





}
