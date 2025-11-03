package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.global.dto.ChatBroadcastPayload;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
import FeedStudy.StudyFeed.global.pubsub.RedisChatPublisher;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.global.type.ChatCategory;
import FeedStudy.StudyFeed.global.type.MembershipStatus;
import FeedStudy.StudyFeed.squad.dto.ChatPageResponse;
import FeedStudy.StudyFeed.squad.dto.SquadChatItemDto;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadChat;
import FeedStudy.StudyFeed.squad.entity.SquadChatImage;
import FeedStudy.StudyFeed.squad.repository.SquadChatRepository;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SquadChatService {

    private final SquadRepository squadRepository;
    private final UserRepository userRepository;
    private final SquadMemberRepository squadMemberRepository;
    private final SquadChatRepository squadChatRepository;
    private final S3FileService s3FileService;
    private final FirebaseMessagingService firebaseMessagingService;
    private final RedisChatPublisher redisChatPublisher;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public SquadChat sendTextMessage(Long squadId, Long userId, String message) {
        ensureJoined(squadId, userId);
        Squad squad = getSquad(squadId);
        User user = getUser(userId);

        insertDateMessageIfNeededSquad(squad);

        SquadChat saved = squadChatRepository.save(SquadChat.text(user, squad, message));

        // 트랜잭션 안에서 payload 구성 (Lazy 문제 방지)
        ChatBroadcastPayload payload = toPayload(saved);

        simpMessagingTemplate.convertAndSend("/sub/squad/" + squadId, payload);

        sendChatPushToOtherMembers(squad, user, message);
        afterCommit(() -> redisChatPublisher.publish(payload));
        return saved;
    }

    @Transactional
    public SquadChat sendImageMessage(Long squadId, Long userId, List<String> imageUrls) {
        ensureJoined(squadId, userId);
        Squad squad = getSquad(squadId);
        User user = getUser(userId);

        insertDateMessageIfNeededSquad(squad);

        List<SquadChatImage> images = imageUrls.stream()
                .map(url -> new SquadChatImage(null, null, url))
                .toList();

        SquadChat saved = squadChatRepository.save(SquadChat.image(user, squad, images));

        ChatBroadcastPayload payload = toPayload(saved);

        sendImagePushToOtherMembers(squad, user);
        afterCommit(() -> redisChatPublisher.publish(payload));
        return saved;
    }

    @Transactional
    public List<String> uploadImagesAndReturnUrls(Long squadId, Long userId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new SquadException(ErrorCode.INVALID_FILE_NAME);
        }
        if (images.size() > 10) {
            throw new SquadException(ErrorCode.IMAGE_UPLOAD_LIMIT_EXCEEDED);
        }
        ensureJoined(squadId, userId);

        Squad squad = getSquad(squadId);
        User user = getUser(userId);

        List<SquadChatImage> uploaded = images.stream().map(file -> {
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null) {
                int dot = original.lastIndexOf('.');
                ext = (dot >= 0 ? original.substring(dot) : "");
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            String url = s3FileService.uploadAndReturnUrl(file, fileName);
            return new SquadChatImage(fileName, original, url);
        }).toList();

        SquadChat saved = squadChatRepository.save(SquadChat.image(user, squad, uploaded));
        ChatBroadcastPayload payload = toPayload(saved);

        afterCommit(() -> redisChatPublisher.publish(payload));
        return uploaded.stream().map(SquadChatImage::getUrl).toList();
    }

    @Transactional
    public SquadChat deleteMessage(Long chatId, Long userId) {
        SquadChat chat = squadChatRepository.findById(chatId)
                .orElseThrow(() -> new SquadException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
        Squad squad = chat.getSquad();

        boolean isAuthor = chat.getUser() != null && chat.getUser().getId().equals(userId);
        boolean isOwner = squad.getUser().getId().equals(userId);
        if (!isAuthor && !isOwner) throw new SquadException(ErrorCode.NOT_CHAT_OWNER);

        chat.delete();
        SquadChat saved = squadChatRepository.save(chat);

        ChatBroadcastPayload payload = toPayload(saved);

        afterCommit(() -> redisChatPublisher.publish(payload));
        return saved;
    }

    @Transactional
    public SquadChat postNotice(Long squadId, Long userId, Long targetChatId) {
        Squad squad = getSquad(squadId);
        if (!squad.getUser().getId().equals(userId)) throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);

        SquadChat target = squadChatRepository.findById(targetChatId)
                .orElseThrow(() -> new SquadException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        squadChatRepository.deleteBySquadIdAndNoticeIsNotNull(squadId);

        SquadChat saved = squadChatRepository.save(SquadChat.notice(target.getUser(), squad, target.getMessage()));

        ChatBroadcastPayload payload = toPayload(saved);

        afterCommit(() -> redisChatPublisher.publish(payload));
        return saved;
    }

    @Transactional(readOnly = true)
    public ChatPageResponse getMessagesPage(Long squadId, Long userId, Long beforeId, int size) {
        ensureJoined(squadId, userId);

        List<SquadChat> chats;
        if (beforeId == null) {
            chats = squadChatRepository.findLatestChats(squadId, PageRequest.of(0, size));
        } else {
            chats = squadChatRepository.findPreviousChats(squadId, beforeId, PageRequest.of(0, size));
        }


        List<SquadChatItemDto> items = chats.stream()
                .map(chat -> SquadChatItemDto.builder()
                        .chatId(chat.getId())
                        .senderId(chat.getUser() != null ? chat.getUser().getId() : null)
                        .nickname(chat.getUser() != null ? chat.getUser().getNickName() : "탈퇴한 회원")
                        .profileImage(chat.getUser() != null ? chat.getUser().getImageUrl() : "avatar_placeholder.png")
                        .type(chat.getType())
                        .message(chat.getMessage())
                        .imageUrls(chat.getImages().stream().map(i -> i.getUrl()).toList())
                        .createdAt(chat.getCreatedAt())
                        .build())
                .toList();

        boolean hasNext = items.size() == size;
        Long nextCursor = hasNext ? items.get(items.size() - 1).getChatId() : null;

        return ChatPageResponse.builder()
                .items(items)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    private void ensureJoined(Long squadId, Long userId) {
        boolean joined = squadMemberRepository.existsBySquadIdAndUserIdAndMembershipStatus(
                squadId, userId, MembershipStatus.JOINED);
        if (!joined) throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
    }

    private void afterCommit(Runnable task) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { task.run(); }
        });
    }

    // 트랜잭션 안에서 호출하여 Lazy 문제 방지
    private ChatBroadcastPayload toPayload(SquadChat chat) {
        Long senderId = (chat.getUser() != null ? chat.getUser().getId() : null);
        String nickname = (chat.getUser() != null ? chat.getUser().getNickName() : "탈퇴한 회원입니다");
        String profile = (chat.getUser() != null ? chat.getUser().getImageUrl() : "avatar_placeholder.png");
        List<String> urls = chat.getImages() == null ? List.of() :
                chat.getImages().stream().map(SquadChatImage::getUrl).toList();

        return ChatBroadcastPayload.builder()
                .chatCategory(ChatCategory.SQUAD)
                .roomId(chat.getSquad().getId())
                .chatId(chat.getId())
                .senderId(senderId)
                .message(chat.getMessage())
                .imageUrls(urls)
                .type(chat.getType())
                .nickname(nickname)
                .profileImage(profile)
                .build();
    }

    // 아래 두 메서드는 실제 구현에 맞게 push 로직을 채우세요.
    private void sendChatPushToOtherMembers(Squad squad, User sender, String message) {
        // TODO: firebaseMessagingService 사용하여 푸시 구현
    }

    private void sendImagePushToOtherMembers(Squad squad, User sender) {
        // TODO: firebaseMessagingService 사용하여 푸시 구현
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new SquadException(ErrorCode.USER_NOT_FOUND));
    }

    private Squad getSquad(Long squadId) {
        return squadRepository.findById(squadId)
                .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));
    }

    private void insertDateMessageIfNeededSquad(Squad squad) {
        LocalDate today = LocalDate.now();

        // [start, next) 구간으로 “오늘” 경계 안전하게 처리
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime next  = today.plusDays(1).atStartOfDay();

        long cnt = squadChatRepository.countByTodayDateChat(squad.getId(), start, next);
        if (cnt > 0) return; // 이미 오늘자 구분선 존재

        SquadChat saved = squadChatRepository.save(SquadChat.date(squad, today));

        // 커밋 후 브로드캐스트 (Lazy 문제 방지 위해 트랜잭션 안에서 payload 생성)
        ChatBroadcastPayload payload = toPayload(saved);
        afterCommit(() -> redisChatPublisher.publish(payload));
    }
}