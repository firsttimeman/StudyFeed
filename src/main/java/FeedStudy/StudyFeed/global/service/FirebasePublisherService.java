package FeedStudy.StudyFeed.global.service;

import FeedStudy.StudyFeed.global.dto.PushRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FirebasePublisherService {
    private final FirebaseMessaging fcm;

    public String postToTopic(PushRequest pushRequest, String topic) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(pushRequest.getTitle())
                .setBody(pushRequest.getBody())
                .build();

        Message msg = Message.builder()
                .setTopic(topic)
                .setNotification(notification)
                .putData("link", pushRequest.getLink())
                .putData("datetime", new SimpleDateFormat("yyyy.MM.dd hh시").format(new Date()))
                .build();
        String id = fcm.send(msg);
        return id;
        //특정 토픽을 구독한 사람 todo 한번더 찾아보기
    }

    public String postToClient(String title, String message, String data, String registrationToken) throws FirebaseMessagingException {

        String[] pushData = data.split(",");
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("id", pushData[0]);
        dataMap.put("type", pushData[1]);


        Notification notification = Notification
                .builder()
                .setTitle(title)
                .setBody(message)
                .build();

        Message msg = Message.builder()
                .setToken(registrationToken)
                .setNotification(notification)
                .putAllData(dataMap)
                .build();

        String id = fcm.send(msg);
        return id;
        //특정 토큰을 지닌 사용자에게
    }

    public void createSubscription(String topic, @RequestBody List<String> registrationTokens) throws FirebaseMessagingException {
        fcm.subscribeToTopic(registrationTokens, topic);
    }

    public void deleteSubscription(String topic, String registrationToken) throws FirebaseMessagingException {
        fcm.unsubscribeFromTopic(Arrays.asList(registrationToken), topic);
    }
}
