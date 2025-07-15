package FeedStudy.StudyFeed.openchat.service;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.global.type.ChatType;
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

    public ChatRoomCreateResponseDto createChatRoom(Long userId, ChatRoomCreateRequestDto dto) {

        User user = getUser(userId);

        ChatRoom chatRoom = ChatRoom.create(user, dto.getTitle(), dto.getTopic(), dto.getDescription(), dto.getMaxParticipants());
        ChatRoomUser chatRoomUser = ChatRoomUser.create(chatRoom, user, true);

        chatRoomRepository.save(chatRoom);
        chatRoomUserRepository.save(chatRoomUser);

        return new ChatRoomCreateResponseDto(chatRoom.getId(), chatRoom.getTitle());
    }

    @Transactional
    public void joinChatRoom(Long roomId, Long userId) {
        ChatRoom room = getChatRoom(roomId);
        User user = getUser(userId);

        // 이미 참여 중인지 확인
        if (chatRoomUserRepository.existsByChatRoomAndUser(room, user)) {
            throw new IllegalStateException("이미 참여 중인 채팅방입니다.");
        }

        room.incrementParticipantCount();
        ChatRoomUser cru = ChatRoomUser.create(room, user, false);
        chatRoomUserRepository.save(cru);
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

        chatRoomUserRepository.delete(cru);
        room.decrementParticipantCount();
    }


    public ChatMessage createTextMessage(Long roomId, Long userId, String content) {
        ChatRoom chatRoom = getChatRoom(roomId);
        User user = getUser(userId);

        insertDateMessageIfNeededChat(chatRoom);

        ChatMessage text = ChatMessage.createText(user, chatRoom, content);
        return chatMessageRepository.save(text);
    }

    public ChatMessage sendImageMessage(Long roomId, Long userId, List<String> imageUrls) {
        ChatRoom chatRoom = getChatRoom(roomId);
        User user = getUser(userId);

        insertDateMessageIfNeededChat(chatRoom);

        List<ChatImage> images = imageUrls.stream()
                .map(url -> new ChatImage(null, null, url))
                .toList();

        ChatMessage image = ChatMessage.image(user, chatRoom, images);
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

        chatMessageRepository.deleteByChatRoomIdAndType(roomId, ChatType.NOTICE);

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


    public void insertDateMessageIfNeededChat(ChatRoom room) {

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
}
