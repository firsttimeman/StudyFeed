//package FeedStudy.StudyFeed.global.service;
//
//import com.google.firebase.messaging.FirebaseMessagingException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class FirebaseMessagingService {
//
//    private final FirebasePublisherService publisherService;
//
//    public boolean sendToUser(Boolean isAlarm,
//                              String token,
//                              String title,
//                              String body,
//                              Map<String, String> dataMap) {
//
//        if (!Boolean.TRUE.equals(isAlarm) || token == null || token.isBlank()) {
//            return false;
//        }
//
//        try {
//            publisherService.postToClient(title, body, dataMap, token);
//            return true;
//        } catch (FirebaseMessagingException e) {
//            log.error("FCM 전송 실패 (단일 사용자) - title: {}", title, e);
//            return false;
//        }
//    }
//
//
//    public void sendToUsers(Boolean isAlarm,
//                            List<String> tokens,
//                            String title,
//                            String body,
//                            Map<String, String> dataMap) {
//
//        if (!Boolean.TRUE.equals(isAlarm) || tokens == null || tokens.isEmpty()) {
//            return;
//        }
//
//        try {
//            publisherService.postToClients(title, body, dataMap, tokens);
//        } catch (FirebaseMessagingException e) {
//            log.error("FCM 전송 실패 (다중 사용자) - title: {}, tokenCount: {}", title, tokens.size(), e);
//        }
//    }
//
//}
