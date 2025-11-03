package FeedStudy.StudyFeed.global.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseMessagingService {

    private final FirebasePublisherService publisherService;

    public boolean sendToUser(Boolean isAlarm, String token, String title, String content, String data) {
        if (Boolean.TRUE.equals(isAlarm) && token != null) {
            try {
                 publisherService.postToClient(title, content, data, token);
                 return true;
            } catch (FirebaseMessagingException e) {
                log.error("FCM 전송 실패 (단일 사용자) - token: {}, title: {}", token, title, e);
            }
        }
        return false;

    }


    public List<String> sendToUsers(Boolean isAlarm, List<String> tokens, String title, String content, String data) {
        if (Boolean.TRUE.equals(isAlarm) && tokens != null && !tokens.isEmpty()) {
            try {
                return publisherService.postToClients(title, content, data, tokens);
            } catch (FirebaseMessagingException e) {
                log.error("FCM 전송 실패 (다중 사용자) - title: {}, tokens: {}", title, tokens.size(), e);
            }
        }
        return Collections.emptyList();
    }

}
