package FeedStudy.StudyFeed.openchat.service;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.openchat.dto.ChatRoomCreateRequestDto;
import FeedStudy.StudyFeed.openchat.dto.ChatRoomCreateResponseDto;
import FeedStudy.StudyFeed.openchat.entity.ChatImage;
import FeedStudy.StudyFeed.openchat.entity.ChatMessage;
import FeedStudy.StudyFeed.openchat.entity.ChatRoom;
import FeedStudy.StudyFeed.openchat.entity.ChatRoomUser;
import FeedStudy.StudyFeed.openchat.repository.ChatImageRepository;
import FeedStudy.StudyFeed.openchat.repository.ChatMessageRepository;
import FeedStudy.StudyFeed.openchat.repository.ChatRoomRepository;
import FeedStudy.StudyFeed.openchat.repository.ChatRoomUserRepository;
import FeedStudy.StudyFeed.openchat.type.ChatRoomUserStatus;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.util.ChatTokenProvider;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {


    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final S3FileService s3FileService;
    private final ChatImageRepository chatImageRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatTokenProvider chatTokenProvider;
    private final FirebaseMessagingService firebaseMessagingService;

    public ChatRoomCreateResponseDto createChatRoom(Long userId, ChatRoomCreateRequestDto dto) {

        User user = getUser(userId);

        ChatRoom chatRoom = ChatRoom.create(user, dto.getTitle(), dto.getTopic(), dto.getDescription(), dto.getMaxParticipants());
        ChatRoomUser chatRoomUser = ChatRoomUser.create(chatRoom, user, true);

        chatRoomRepository.save(chatRoom);
        chatRoomUserRepository.save(chatRoomUser);

        String openChatToken = chatTokenProvider.createOpenChatToken(user, chatRoom);


        return new ChatRoomCreateResponseDto(chatRoom.getId(), chatRoom.getTitle(), openChatToken);
    }

    @Transactional
    public Map<String, String> joinChatRoomWithToken(Long roomId, Long userId) {
        ChatRoom room = getChatRoom(roomId);
        User user = getUser(userId);

        // 이미 참여 중인지 확인
        ChatRoomUser cru = chatRoomUserRepository.findByChatRoomAndUser(room, user).orElse(null);

        if (cru != null) {
            if (cru.getStatus() == ChatRoomUserStatus.JOINED) {
                // 이미 참여 중
                String openChatToken = chatTokenProvider.createOpenChatToken(user, room);
                return Map.of("status", "already joined", "chatToken", openChatToken);
            } else if (cru.getStatus() == ChatRoomUserStatus.LEFT) {
                // 재입장
                cru.setStatus(ChatRoomUserStatus.JOINED);
                room.incrementParticipantCount();
                String openChatToken = chatTokenProvider.createOpenChatToken(user, room);
                return Map.of("status", "rejoined", "chatToken", openChatToken);
            } else if (cru.getStatus() == ChatRoomUserStatus.KICKED) {
                throw new IllegalStateException("강퇴된 유저는 재입장할 수 없습니다.");
            }
        }

        // 처음 입장
        ChatRoomUser newUser = ChatRoomUser.create(room, user, false);
        chatRoomUserRepository.save(newUser);
        room.incrementParticipantCount();

        String openChatToken = chatTokenProvider.createOpenChatToken(user, room);
        return Map.of("status", "joined", "chatToken", openChatToken);
    }


    @Transactional
    public void leaveChatRoom(Long roomId, Long userId) {
        ChatRoom room = getChatRoom(roomId);
        User user = getUser(userId);

        ChatRoomUser cru = chatRoomUserRepository.findByChatRoomAndUser(room, user)
                .orElseThrow(() -> new IllegalStateException("참여 중인 채팅방이 아닙니다."));

        if (room.getOwner().equals(user)) {
            throw new IllegalStateException("방장은 채팅방을 나갈 수 없습니다.");
        }

        cru.setStatus(ChatRoomUserStatus.LEFT);
        room.decrementParticipantCount();
    }

    public Map<String, String> refreshChatToken(Long roomId, User user) {

        ChatRoom chatRoom = getChatRoom(roomId);

        boolean isParticipant = chatRoomUserRepository.existsByChatRoomAndUser(chatRoom, user);
        if(!isParticipant) {
            throw new IllegalArgumentException("해당 채팅방에 참여하고 있지 않다");
        }

        String openChatToken = chatTokenProvider.createOpenChatToken(user, chatRoom);
        return Map.of("chatToken", openChatToken);

    }


    public void kickParticipant(Long roomId, User user) {

        ChatRoom chatRoom = getChatRoom(roomId);
        User owner = chatRoom.getOwner();

        if(!owner.getId().equals(user.getId())) {
            ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(chatRoom, user)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 이 채팅방에 속해있지 않습니다."));

            chatRoomUser.setStatus(ChatRoomUserStatus.KICKED);
            chatRoom.decrementParticipantCount();
        } else {
            throw new IllegalArgumentException("방장은 강퇴할 수 없습니다.");
        }


    }


    public ChatMessage createTextMessage(Long roomId, Long userId, String content) {
        ChatRoom chatRoom = getChatRoom(roomId);
        User user = getUser(userId);

        insertDateMessageIfNeededChat(chatRoom);

        ChatMessage text = ChatMessage.createText(user, chatRoom, content);

        sendOpenChatPushToOtherMembers(chatRoom, user, content); // todo n+1 발생 가능

        return chatMessageRepository.save(text);
    }

    public ChatMessage sendImageMessage(Long roomId, Long userId, List<String> imageUrls) {
        ChatRoom chatRoom = getChatRoom(roomId);//todo n+1
        User user = getUser(userId); //todo n+1
        //	•	① getChatRoom(roomId)
        //→ 내부에서 chatRoomRepository.findById(...) 같이 단일 조회만 하면 N+1 아님.
        //→ 단, chatRoom.getUsers() 같은 Lazy 연관관계 컬렉션을 순회하면 N+1 발생.

        //	•	② getUser(userId)
        //→ 단일 조회면 문제 없음. 역시 연관된 엔티티(Lazy) 접근할 때만 N+1.

        insertDateMessageIfNeededChat(chatRoom);

        List<ChatImage> images = imageUrls.stream()
                .map(url -> new ChatImage(null, null, url))
                .toList();

        ChatMessage image = ChatMessage.image(user, chatRoom, images);

        sendImagePushOpenChatToOtherMembers(chatRoom, user); //todo n+1
        //	•	③ sendImagePushOpenChatToOtherMembers(chatRoom, user)
        //→ 여기서 chatRoom.getChatRoomUsers() 같은 컬렉션을 돌면서 각 User에 접근한다면 N+1 가능성이 큼.
        //→ @OneToMany(fetch = LAZY) 기본값일 테니까요.

        return chatMessageRepository.save(image);
    }

    public List<String> uploadImagesAndReturnUrls(Long roomId, Long userId, List<MultipartFile> images) {
        if (images.size() > 10) {
            throw new IllegalArgumentException("최대 이미지 등록 초과");
        }

        ChatRoom chatRoom = getChatRoom(roomId);
        User user = getUser(userId);

        List<ChatImage> uploaded = images.stream().map(file -> {
            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            String originalFilename = file.getOriginalFilename();
            String fullUrl = s3FileService.uploadAndReturnUrl(file, filename);
            return new ChatImage(filename, originalFilename, fullUrl);
        }).toList();

        ChatMessage chat = ChatMessage.image(user, chatRoom, uploaded);
        uploaded.forEach(image -> image.initChatMessage(chat));

        chatMessageRepository.save(chat);

        chatImageRepository.saveAll(uploaded);

        return uploaded.stream().map(ChatImage::getUrl).toList();
    }

    public ChatMessage postNotice(Long roomId, Long userId, Long targetMessageId) {
        ChatRoom room = getChatRoom(roomId);

        if (!room.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("채팅방의 주인이 아닙니다.");
        }

        ChatMessage target = chatMessageRepository.findById(targetMessageId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메세지를 찾을수가 없습니다."));

        chatMessageRepository.deleteByChatRoomIdAndNoticeIsNotNull(roomId);

        ChatMessage notice = ChatMessage.notice(target.getSender(), room, target.getContent());
        return chatMessageRepository.save(notice);
    }

    public ChatMessage deleteMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메세지를 찾을수가 없습니다."));

        ChatRoom room = message.getChatRoom();
        boolean isAuthor = message.getSender().getId().equals(userId);
        boolean isOwner = room.getOwner().getId().equals(userId);

        if (!isAuthor && !isOwner) {
            throw new IllegalArgumentException("채팅방의 주인이 아닙니다.");
        }

        message.softDelete();
         return chatMessageRepository.save(message);
    }


    public List<ChatMessage> loadRecentMessages(Long roomId, Pageable pageable) {
        return chatMessageRepository.findLatestMessages(roomId, pageable);

    }

    public List<ChatMessage> loadPreviousMessages(Long roomId, Long lastMessageId, Pageable pageable) {
        return chatMessageRepository.findPreviousMessages(roomId, lastMessageId, pageable);
    }


    private void insertDateMessageIfNeededChat(ChatRoom room) {

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        if(chatMessageRepository.countByTodayDateChat(room.getId(), start, end) == 0) {
            ChatMessage date = ChatMessage.date(room, today);
            chatMessageRepository.save(date);
        }

    }


    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    private ChatRoom getChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 못찾았습니다."));

    }

    private void sendOpenChatPushToOtherMembers(ChatRoom room, User sender, String message) {
        String title = room.getTitle();
        String body = sender.getNickName() + " : " + message;
        String data = room.getId() + ",chat";

        List<String> fcmTokens = room.getUsers().stream()
                .map(u -> u.getUser()) //todo N+1 발생
                .filter(u -> !u.getId().equals(sender.getId()))
                .filter(u -> Boolean.TRUE.equals(u.getChatroomAlarm()))
                .map(u -> u.getFcmToken())
                .filter(token -> token != null && !token.isBlank())
                .toList();

        //여기서 room.getUsers()가 LAZY 컬렉션이면 사용자 수 M만큼 추가로 ChatRoomUser(컬렉션 초기화) +
        // 각 ChatRoomUser.getUser() 접근 시 유저당 1쿼리가 연쇄적으로 나가서 전형적인 N+1이 됩니다.

        if (!fcmTokens.isEmpty()) {
            firebaseMessagingService.sendCommentNotificationToMany(true, fcmTokens, title, body, data);
        }
    }


    private void sendImagePushOpenChatToOtherMembers(ChatRoom room, User sender) {
        String title = room.getTitle();
        String body = sender.getNickName() + "님이 사진을 보냈어요 📸";
        String data = room.getId() + ",chat";

        List<String> fcmTokens = room.getUsers().stream()// todo 컬렉션 초기화 1회(ROOM_USER 목록 로딩)
                .map(ChatRoomUser::getUser) // todo N+1 발생
                //	•	ChatRoomUser.user도 보통 @ManyToOne(fetch = LAZY)라서, 스트림에 있는 각 ChatRoomUser마다 getUser() 접근 시 유저를 로드하는 쿼리가 N번 추가로 나갑니다.
                //예(반복): select u from User u where u.id = :userId
                //
                //즉 패턴은 전형적인 N+1:
                //	•	1 (컬렉션 로드) + N (각 원소에서 LAZY 연관 로드)
                .filter(user -> !user.getId().equals(sender.getId()))
                .filter(user -> Boolean.TRUE.equals(user.getChatroomAlarm()))
                .map(User::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .toList();


        if (!fcmTokens.isEmpty()) {
            firebaseMessagingService.sendCommentNotificationToMany(true, fcmTokens, title, body, data);
        }

    }
}
