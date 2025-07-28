package FeedStudy.StudyFeed.global.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {

    private final FirebasePublisherService publisherService;

    public String sendCommentNotification(Boolean isAlarm, String token, String title, String content, String data) {
        if (isAlarm != null && isAlarm && token != null) {
            try {
                return publisherService.postToClient(title, content, data, token);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
        }
        return null;

    }


    public List<String> sendCommentNotificationToMany(Boolean isAlarm, List<String> tokens, String title, String content, String data) {
        if (isAlarm != null && isAlarm && tokens != null && !tokens.isEmpty()) {
            try {
                return publisherService.postToClients(title, content, data, tokens);
            } catch (FirebaseMessagingException e) {
                e.printStackTrace(); // 필요 시 logger로 변경
            }
        }
        return Collections.emptyList();
    }

}
