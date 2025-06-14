//package FeedStudy.StudyFeed.squad.service;
//
//
//import FeedStudy.StudyFeed.global.service.S3FileService;
//import FeedStudy.StudyFeed.squad.entity.Squad;
//import FeedStudy.StudyFeed.squad.entity.SquadChat;
//import FeedStudy.StudyFeed.squad.repository.SquadChatRepository;
//import FeedStudy.StudyFeed.squad.repository.SquadRepository;
//import FeedStudy.StudyFeed.user.entity.User;
//import FeedStudy.StudyFeed.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class SquadChatService {
//
//    private final SquadRepository squadRepository;
//    private final UserRepository userRepository;
//    private final SquadChatRepository squadChatRepository;
//    private final S3FileService s3FileService;
//
//    public void sendTextMessage(Long squadId, Long userId, String message) {
//        Squad squad = squadRepository.findById(squadId).orElseThrow(() -> new RuntimeException("Squad not found"));
//        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
//
//        insertDateMessageIfNeeded(squad);
//
//        SquadChat squadChat = SquadChat.text(user, squad, message);
//        squadChatRepository.save(squadChat);
//    }
//
////    public void sendImageMessage(Long squadId, Long userId, List<MultipartFile> images) {
////
////        if(images.size() > 10) {
////            throw new IllegalArgumentException("최대 10장까지 업로드 할수 있습니다.");
////        }
////
////
////        Squad squad = squadRepository.findById(squadId).orElseThrow(() -> new RuntimeException("Squad not found"));
////        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
////
////        insertDateMessageIfNeeded(squad);
////
////        images.stream().map(file -> {
////            s3FileService.upload(file);
////        })
////
////    }
//
//
//    private void insertDateMessageIfNeeded(Squad squad) {
//
//        LocalDate today = LocalDate.now();
//
//        LocalDateTime start = today.atStartOfDay();
//        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);
//
//
//        if(!squadChatRepository.existsByTodayDateChat(squad.getId(), start, end)) {
//            SquadChat dateChat = SquadChat.date(squad, today);
//            squadChatRepository.save(dateChat);
//        }
//    }
//}
