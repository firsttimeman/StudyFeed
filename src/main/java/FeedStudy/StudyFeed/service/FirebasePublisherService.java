package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.dto.PushRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FirebasePublisherService {
    private final FirebaseMessaging fcm;

    public String postToTopic(PushRequest pushRequest, String topic) throws FirebaseMessagingException {
        Notification notification = Notification.builder().setTitle(pushRequest.getTitle()).setBody(pushRequest.getBody()).build();
        Message msg = Message.builder()
                .setTopic(topic)
                .setNotification(notification)
                .putData("link", pushRequest.getLink())
                .putData("datetime", new SimpleDateFormat("yyyy.MM.dd hh시").format(new Date()))
                .build();
        String id = fcm.send(msg);
        return id;
    }

    public String postToClient(String title, String message, String registrationToken) throws FirebaseMessagingException {
        Notification notification = Notification.builder().setTitle(title).setBody(message).build();
        Message msg = Message.builder()
                .setToken(registrationToken)
                .setNotification(notification)
                .putData("body", message)
                .build();

        String id = fcm.send(msg);
        return id;
    }

    public void createSubscription(String topic, @RequestBody List<String> registrationTokens) throws FirebaseMessagingException {
        fcm.subscribeToTopic(registrationTokens, topic);
    }

    public void deleteSubscription(String topic, String registrationToken) throws FirebaseMessagingException {
        fcm.unsubscribeFromTopic(Arrays.asList(registrationToken), topic);
    }
}
