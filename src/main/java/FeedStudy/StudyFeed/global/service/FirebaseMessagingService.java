package FeedStudy.StudyFeed.global.service;

import FeedStudy.StudyFeed.user.entity.User;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
