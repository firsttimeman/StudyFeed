package FeedStudy.StudyFeed.squad.service;


import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.ChatType;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadChat;
import FeedStudy.StudyFeed.squad.entity.SquadChatImage;
import FeedStudy.StudyFeed.squad.repository.SquadChatRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SquadChatService {

    private final SquadRepository squadRepository;
    private final UserRepository userRepository;
    private final SquadChatRepository squadChatRepository;
    private final S3FileService s3FileService;
    private final FirebaseMessagingService firebaseMessagingService;

    public SquadChat sendTextMessage(Long squadId, Long userId, String message) {
        Squad squad = getSquad(squadId);
        User user = getUser(userId);

        insertDateMessageIfNeededSquad(squad);

        SquadChat squadChat = SquadChat.text(user, squad, message);

        sendChatPushToOtherMembers(squad, user, message);

        return squadChatRepository.save(squadChat);



    }


    public SquadChat sendImageMessage(Long squadId, Long userId, List<String> imageUrls) {
        Squad squad = getSquad(squadId);
        User user = getUser(userId);

        insertDateMessageIfNeededSquad(squad);

        List<SquadChatImage> images = imageUrls.stream()
                .map(url -> new SquadChatImage(null, null, url))
                .toList();

        SquadChat chat = SquadChat.image(user, squad, images);

        sendImagePushToOtherMembers(squad, user);
        return squadChatRepository.save(chat);
    }

    private void sendImagePushToOtherMembers(Squad squad, User sender) {
        String title = squad.getTitle();
        String body = sender.getNickName() + "님이 사진을 보냈어요 📸";
        String data = squad.getId() + ",squad";

        List<String> fcmTokens = squad.getMembers().stream()
                .filter(m -> m.getAttendanceStatus() == AttendanceStatus.JOINED)
                .filter(m -> !m.getUser().getId().equals(sender.getId()))
                .map(m -> m.getUser())
                .filter(u -> Boolean.TRUE.equals(u.getSquadChatAlarm()))
                .map(u -> u.getFcmToken())
                .filter(obj -> Objects.nonNull(obj))
                .toList();

        if (!fcmTokens.isEmpty()) {
            firebaseMessagingService.sendCommentNotificationToMany(true, fcmTokens, title, body, data);
        }

    }

    public List<String> uploadImagesAndReturnUrls(Long squadId, Long userId, List<MultipartFile> images) {

        if (images.size() > 10) {
            throw new SquadException(ErrorCode.IMAGE_UPLOAD_LIMIT_EXCEEDED);
        }


        Squad squad = getSquad(squadId);
        User user = getUser(userId);


        List<SquadChatImage> uploaded = images.stream().map(file -> {
            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            String originalFilename = file.getOriginalFilename();
            String fullUrl = s3FileService.uploadAndReturnUrl(file, fileName);
            return new SquadChatImage(fileName, originalFilename, fullUrl);
        }).toList();

        SquadChat chat = SquadChat.image(user, squad, uploaded);
        uploaded.forEach(image -> image.initSquadChat(chat));
        squadChatRepository.save(chat);

        return uploaded.stream().map(SquadChatImage::getUrl).toList();
    }




    public SquadChat deleteMessage(Long chatId, Long userId) {
        SquadChat chat = squadChatRepository.findById(chatId)
                .orElseThrow(() -> new SquadException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        Squad squad = chat.getSquad();

        boolean isAuthor = chat.getUser().getId().equals(userId);
        boolean isSquadOwner = squad.getUser().getId().equals(userId);

        if (!isAuthor && !isSquadOwner) {
            throw new SquadException(ErrorCode.NOT_CHAT_OWNER);
        }


        chat.delete();
        return squadChatRepository.save(chat);
    }

    public SquadChat postNotice(Long squadId, Long userId, Long targetChatId) {
        Squad squad = getSquad(squadId);

        if (!squad.getUser().getId().equals(userId)) {
            throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
        }

        SquadChat targetChat = squadChatRepository.findById(targetChatId)
                .orElseThrow(() -> new SquadException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        squadChatRepository.deleteBySquadIdAndNoticeIsNotNull(squadId);

        SquadChat notice = SquadChat.notice(targetChat.getUser(), squad, targetChat.getMessage());


        return squadChatRepository.save(notice);
    }


    public List<SquadChat> loadRecentMessages(Long squadId) {

        return squadChatRepository.findLatestChats(squadId, PageRequest.of(0, 20));
    }


    public List<SquadChat> loadPreviousMessages(Long squadId, Long lastId) {
        return squadChatRepository.findPreviousChats(squadId, lastId, PageRequest.of(0, 20));
    }

    private void insertDateMessageIfNeededSquad(Squad squad) {

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);


        if(squadChatRepository.countByTodayDateChat(squad.getId(), start, end) == 0) {
            SquadChat dateChat = SquadChat.date(squad, today);
            squadChatRepository.save(dateChat);
        }

    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new SquadException(ErrorCode.USER_NOT_FOUND));
    }

    private Squad getSquad(Long squadId) {
        return squadRepository.findById(squadId)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));
    }

    private void sendChatPushToOtherMembers(Squad squad, User sender, String message) {
        String title = squad.getTitle();
        String body = sender.getNickName() + " : " + message;
        String data = squad.getId() + ",squad";

        List<String> fcmTokens = squad.getMembers().stream()
                .filter(m -> m.getAttendanceStatus() == AttendanceStatus.JOINED)
                .filter(m -> !m.getUser().getId().equals(sender.getId()))
                .map(m -> m.getUser())
                .filter(u -> Boolean.TRUE.equals(u.getSquadChatAlarm()))
                .map(u -> u.getFcmToken())
                .filter(obj -> Objects.nonNull(obj))
                .toList();

        if (!fcmTokens.isEmpty()) {
            firebaseMessagingService.sendCommentNotificationToMany(true, fcmTokens, title, body, data);
        }
    }


}
