package FeedStudy.StudyFeed.global.service;

import FeedStudy.StudyFeed.user.entity.User;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {

    private final FirebasePublisherService publisherService;

    public String sendCommentNotification(User targetUser, String title, String content) {
        String fcmToken = targetUser.getFcmToken();

        if (targetUser.getReceiveFeedAlarm() && fcmToken != null) {
            try {
                return publisherService.postToClient(title, content, fcmToken);
            } catch (FirebaseMessagingException e) {
                throw new RuntimeException("푸시 알림 발송이 실패했습니다.", e);
            }
        }

        return null;

    }

}
