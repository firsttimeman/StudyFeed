//package FeedStudy.StudyFeed.global.service;
//
//import FeedStudy.StudyFeed.global.dto.PushRequest;
//import com.google.firebase.messaging.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class FirebasePublisherService {
//    private final FirebaseMessaging fcm;
//
//    private static final DateTimeFormatter TOPIC_TIME_FORMAT =
//            DateTimeFormatter.ofPattern("yyyy.MM.dd HH시");
//
//    public String postToTopic(PushRequest pushRequest, String topic) throws FirebaseMessagingException {
//        Notification notification = buildNotification(
//                pushRequest.getTitle(),
//                pushRequest.getBody()
//        );
//
//
//        Message msg = Message.builder()
//                .setTopic(topic)
//                .setNotification(notification)
//                .putData("link", Optional.ofNullable(pushRequest.getLink()).orElse(""))
//                .putData("datetime", LocalDateTime.now().format(TOPIC_TIME_FORMAT))
//                .build();
//
//
//        return fcm.send(msg);
//    }
//
//    public String postToClient(String title,
//                               String body,
//                               Map<String, String> dataMap,
//                               String registrationToken) throws FirebaseMessagingException {
//
//        Notification notification = buildNotification(title, body);
//
//        Message msg = Message.builder()
//                .setToken(registrationToken)
//                .setNotification(notification)
//                .putAllData(safeDataMap(dataMap))
//                .build();
//
//        return fcm.send(msg);
//    }
//
//
//    public void postToClients(String title,
//                                      String body,
//                                      Map<String, String> dataMap,
//                                      List<String> registrationTokens) throws FirebaseMessagingException {
//
//        Notification notification = buildNotification(title, body);
//
//        MulticastMessage msg = MulticastMessage.builder()
//                .addAllTokens(registrationTokens)
//                .setNotification(notification)
//                .putAllData(safeDataMap(dataMap))
//                .build();
//
//        BatchResponse batchResponse = fcm.sendMulticast(msg);
//
//
//        log.info("FCM success={}, fail={}",
//                batchResponse.getSuccessCount(),
//                batchResponse.getFailureCount());
//
//    }
//
//    public void createSubscription(String topic, List<String> registrationTokens) throws FirebaseMessagingException {
//        fcm.subscribeToTopic(registrationTokens, topic);
//    }
//
//    public void deleteSubscription(String topic, String registrationToken) throws FirebaseMessagingException {
//        fcm.unsubscribeFromTopic(Arrays.asList(registrationToken), topic);
//    }
//
//    private Notification buildNotification(String title, String body) {
//        return Notification.builder()
//                .setTitle(title)
//                .setBody(body)
//                .build();
//    }
//
//    private Map<String, String> safeDataMap(Map<String, String> dataMap) {
//        return dataMap == null ? Collections.emptyMap() : dataMap;
//    }
//}
