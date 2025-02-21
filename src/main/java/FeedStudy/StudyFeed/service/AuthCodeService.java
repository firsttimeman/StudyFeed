package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.exception.exceptiontype.AuthCodeException;
import FeedStudy.StudyFeed.exception.ErrorCode;
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

    public void saveAuthCode(String email, String authCode) {
        String key = "authCode:" + email; // 공백 제거
        redisTemplate.opsForValue().set(key, authCode, 10, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("emailByAuthCode:" + authCode, email, 10, TimeUnit.MINUTES);

        log.info("✅ 저장된 키 from saveAuthCode: " + key);
        log.info("✅ 저장된 인증 코드 saveAuthCOde: " + authCode);




    }


    public String getEmailByAuthCode(String authCode) {
        String email = redisTemplate.opsForValue().get("emailByAuthCode: " + authCode);
        if(email == null) {
            throw new AuthCodeException(ErrorCode.AUTH_CODE_MISMATCH);
        }
        return email;
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


    public boolean checkAuthCode(String email, String authCode) {
        String storedCode = redisTemplate.opsForValue().get("authCode: " + email);
        System.out.println("✅ 저장된 인증 코드: " + storedCode);
        System.out.println("✅ 사용자가 입력한 인증 코드: " + authCode);
        if(storedCode != null && storedCode.equals(authCode)) {
            redisTemplate.delete("authCode: " + email);
            return true;
        }



        return false;
    }
}
