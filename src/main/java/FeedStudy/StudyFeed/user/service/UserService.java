package FeedStudy.StudyFeed.user.service;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.global.utils.NickNameUtils;
import FeedStudy.StudyFeed.openchat.entity.ChatImage;
import FeedStudy.StudyFeed.openchat.entity.ChatMessage;
import FeedStudy.StudyFeed.openchat.entity.ChatRoom;
import FeedStudy.StudyFeed.openchat.entity.ChatRoomUser;
import FeedStudy.StudyFeed.openchat.repository.ChatMessageRepository;
import FeedStudy.StudyFeed.openchat.repository.ChatRoomRepository;
import FeedStudy.StudyFeed.openchat.repository.ChatRoomUserRepository;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadChat;
import FeedStudy.StudyFeed.squad.entity.SquadChatImage;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.squad.repository.SquadChatRepository;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.dto.DescriptionRequestDto;
import FeedStudy.StudyFeed.user.dto.NickNameCheckResponse;
import FeedStudy.StudyFeed.user.dto.ProfileImageUpdateDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.RefreshRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 1. 유저 회원가입
 * 2. 유저 중복확인, 전화번호 중복확인 필요
 * 3. 회원가입이 정상적이면 이메일로 계정 활성화 링크를 보내 활성을 해야 로그인 가능
 * <p>
 * 1. 로그인
 * 이메일, 비밀번호로 로그인(JWT 발행)
 * <p>
 * 1. 비밀번호 찾기
 * 이메일로 비밀번호 재설정 페이지 보내거나, 임시 비밀번호 발급
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    public final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RefreshRepository refreshRepository;

    private final S3FileService s3FileService;
    private final SquadChatRepository squadChatRepository;
    private final FeedRepository feedRepository;
    private final FeedImageRepository feedImageRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final SquadMemberRepository squadMemberRepository;
    private final SquadRepository squadRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;



    public String makeNickName() {

        String generateNickName = generateUniqueNickName();
        return generateNickName;
    }

    public void updateNickname(User user, String nickName) {

        if (userRepository.existsByIdNotAndNickName(user.getId(), nickName)) {
            throw new MemberException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.setNickName(nickName);
        userRepository.save(user);
    }

    public NickNameCheckResponse checkNickname(String rawNickname) {
       String normalized = rawNickname == null ? "" : rawNickname.trim().replaceAll("\\s{2,}", " ");

       String regex = "^[a-zA-Z0-9가-힣\\s]{2,12}$";
       if(!normalized.matches(regex)) {
           return NickNameCheckResponse.builder()
                   .valid(false)
                   .available(false)
                   .message("2~12자, 한글/영문/숫자/공백만 사용할 수 있어요.")
                   .normalized(normalized)
                   .build();
       }

       if(normalized.matches("^[0-9]+$")) {
           return NickNameCheckResponse.builder()
                   .valid(false).available(false)
                   .message("숫자만으로는 닉네임을 만들 수 없어요.")
                   .normalized(normalized)
                   .build();

       }

        if (normalized.matches("^(.)\\1{3,}$")) {
            return NickNameCheckResponse.builder()
                    .valid(false).available(false)
                    .message("같은 문자를 4번 이상 반복할 수 없어요.")
                    .normalized(normalized)
                    .build();
        }

        boolean exists = userRepository.existsByNickName(normalized);
        if (exists) {
            return NickNameCheckResponse.builder()
                    .valid(true).available(false)
                    .message("이미 사용 중인 닉네임이에요.")
                    .normalized(normalized)
                    .build();
        }

        return NickNameCheckResponse.builder()
                .valid(true).available(true)
                .message("사용 가능한 닉네임이에요!")
                .normalized(normalized)
                .build();
    }

    public boolean hasNickName(User user) {
        return !(user.getNickName() == null || user.getNickName().equals(""));
    }

    @Transactional
    public void fcmTokenRefresh(User user, String fcmToken) {
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }



    public Map<String, String> getNickname(User user) {
        Map<String, String> map = new HashMap<>();
        map.put("nickname", user.getNickName() == null ? "" : user.getNickName());
        return map;
    }

    @Transactional
    public User modifyDescription(DescriptionRequestDto dto, User user) {
        user.setDescription(dto.getDescription());
        return userRepository.save(user);

    }

    @Transactional
    public User changeProfileImage(User user, ProfileImageUpdateDto dto) {

        final String DEFAULT_IMAGE = "avatar_placeholder.png";
        final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

        String oldUrl = user.getImageUrl();

        if(dto.isResetToDefault()) {
            if(oldUrl != null && !oldUrl.isBlank() && !oldUrl.endsWith(DEFAULT_IMAGE)) {
                String oldFileName = oldUrl.substring(oldUrl.lastIndexOf('/') + 1);
                s3FileService.delete(oldFileName);
            }
            user.setImageUrl(DEFAULT_IMAGE);
            return userRepository.save(user);
        }

        MultipartFile file = dto.getProfileImage();
        if(file == null || file.isEmpty()) return user;

        String contentType = file.getContentType();
        if(contentType == null || !ALLOWED.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("JPEG/PNG/GIF/WEBP 형식의 이미지 파일만 업로드할 수 있어요.");
        }

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains(".")) ?
                original.substring(original.lastIndexOf('.')) : "";
        String newFileName = UUID.randomUUID() + ext;



        String newUrl = s3FileService.uploadAndReturnUrl(file, newFileName);
        user.setImageUrl(newUrl);
        User saved = userRepository.save(user);



        if (oldUrl != null && !oldUrl.isBlank() && !oldUrl.endsWith(DEFAULT_IMAGE)) {
            String oldFileName = oldUrl.substring(oldUrl.lastIndexOf('/') + 1);
            try {
                s3FileService.delete(oldFileName);
            } catch (Exception e) {
                log.warn("기존 이미지 삭제 실패: {}", oldFileName, e);
            }
        }

        return saved;

    }

    @Transactional
    public User toggleAllAlarm(User user, boolean enabled) {

        user.setFeedAlarm(enabled);
        user.setFeedLikeAlarm(enabled);
        user.setSquadChatAlarm(enabled);
        user.setChatroomAlarm(enabled);
        user.setSquadNotifyAlarm(enabled);
        return userRepository.save(user);

    }

    //todo 여기 설정 피드랑 스쿼드 다하고 나서 할것
    @Transactional
    public void deleteUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new MemberException(ErrorCode.USER_NOT_FOUND);
        }

        String profileImageUrl = user.getImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isBlank() &&
                !profileImageUrl.equals("avatar_placeholder.png")) {
            String fileName = profileImageUrl.substring(profileImageUrl.lastIndexOf("/") + 1);
            s3FileService.delete(fileName);
        }

        List<FeedComment> otherComments = feedCommentRepository.findByUser(user).stream()
                .filter(comment -> !comment.getFeed().getUser().equals(user)) // todo LAZY 접근 2단계 → N+1
                .toList();

        for (FeedComment otherComment : otherComments) {
            otherComment.setUser(null);
        } // 다른 사람의 피드에서 작성자만 null 처리 글들은 삭제 X

        feedLikeRepository.deleteAllByUser(user); // 다른 사용자의 피드에서 like를 없애는 기능

        List<Feed> userFeeds = feedRepository.findByUser(user);
        List<FeedImage> feedImages = feedImageRepository.findAllByFeedIn(userFeeds);
        for (FeedImage feedImage : feedImages) {
            s3FileService.delete(feedImage.getUniqueName());
        }

        feedRepository.deleteAll(userFeeds); // 최종적으로 탈퇴할 유저의 피드를 전부 삭제를 하는 기능 // todo n+1 문제 발생 가능
        //이미 FeedImage는 선삭제했지만, Feed가 다른 연관(댓글 등)과 영속성 전이/고아제거로 묶여 있으면 JPA가 개별 엔티티를 로딩하며 삭제를 수행하는 과정에서 추가 LAZY 로딩이 발생할 수 있습니다.

        // 7. 유저가 참여한 스쿼드의 채팅들에서 작성자만 null 처리 + 이미지 삭제
        List<SquadMember> joinedSquads = squadMemberRepository.findByUser(user);
        for (SquadMember member : joinedSquads) {
            Squad squad = member.getSquad();  // todo LAZY → N

            List<SquadChat> userChats = squadChatRepository.findBySquadAndUser(squad, user); //todo 스쿼드마다 조회 → N
            for (SquadChat chat : userChats) {
                for (SquadChatImage image : chat.getImages())  { // todo  이미지 LAZY → N
                    s3FileService.delete(image.getUniqueName());
                }
                chat.setUser(null); // 작성자 null 처리
            }

            squadMemberRepository.delete(member); // 유저 탈퇴 처리 // Todo n+1 문제는 아니지만
            //  반복 삭제로 쿼리 수 증가(퍼포먼스 악화). deleteAllInBatch(…)나 deleteAllByUser(…) 같은 벌크 메서드로 줄일 수 있습니다.


        }

        List<Squad> createdFeeds = squadRepository.findByUser(user);
        for (Squad squad : createdFeeds) {
            List<SquadChat> chats = squadChatRepository.findBySquad(squad);
            for (SquadChat chat : chats) {
                for (SquadChatImage image : chat.getImages()) {
                    s3FileService.delete(image.getUniqueName());
                }
            }
            squadChatRepository.deleteAll(chats);
            squadRepository.delete(squad);
        }

        List<ChatRoomUser> joinedRooms = chatRoomUserRepository.findByUser(user);
        for (ChatRoomUser roomUser : joinedRooms) {
            ChatRoom room = roomUser.getChatRoom();         // todo LAZY → N

            List<ChatMessage> userMessage = chatMessageRepository.findByChatRoomAndSender(room, user); // todo 방마다 조회 → N
            for (ChatMessage message : userMessage) {
                for (ChatImage image : message.getImages()) { // todo 이미지 LAZY → N
                    s3FileService.delete(image.getUniqueName());
                }
                message.setSender(null);
            }
            chatRoomUserRepository.delete(roomUser);
            //Todo chatRoomUserRepository.delete(roomUser);  // 루프 내 개별 삭제
            //	전형적 N+1은 아니지만, 반복 삭제로 쿼리 수 증가(퍼포먼스 악화). deleteAllInBatch(…)나 deleteAllByUser(…) 같은 벌크 메서드로 줄일 수 있습니다.
        }

        List<ChatRoom> createdRooms = chatRoomRepository.findByOwner(user);
        for (ChatRoom room : createdRooms) { // todo room.getMessages() LAZY → 방 수만큼 조회
            for (ChatMessage message : room.getMessages()) {
                for (ChatImage image : message.getImages()) { // todo 메시지당 이미지 LAZY → 추가 N
                    s3FileService.delete(image.getUniqueName());
                }
            }
            chatRoomRepository.delete(room); // todo 개별 삭제 루프
        }

        refreshRepository.deleteRefreshToken(user.getEmail());

        userRepository.delete(user);

    }

    private String generateUniqueNickName() {
        for(int i = 0; i < 100; i++) {
            String nickname = NickNameUtils.generateNickname();
            if(!userRepository.existsByNickName(nickname)) {
                return nickname;
            }
        }

        throw new MemberException(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }
}
