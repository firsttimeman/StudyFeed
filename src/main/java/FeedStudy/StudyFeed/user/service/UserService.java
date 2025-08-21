package FeedStudy.StudyFeed.user.service;

import FeedStudy.StudyFeed.auth.service.AuthCodeService;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.jwt.JwtUtil;
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
import FeedStudy.StudyFeed.user.dto.ProfileImageUpdateDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.BlackListRepository;
import FeedStudy.StudyFeed.user.repository.RefreshRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import com.google.api.pathtemplate.ValidationException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    // public void RegisterUser(String email) throws MessagingException {
    //
    // String authCode = authCodeService.generateAuthCode();
    // authCodeService.saveAuthCode(email, authCode);
    // mailService.sendVerifyMail(email, authCode);
    //
    // //1. 해당되는 이메일로 인증코드를 보낸다. 보내면서 레디스에서 코드를 저장한다.
    // }

    // public void activateUser(SignUpRequestDto signUpRequestDto) {
    //
    // String email = signUpRequestDto.getEmail();
    // String authCode = signUpRequestDto.getAuthcode();
    //
    // if (authCodeService.checkAuthCode(email, authCode)) {
    // throw new AuthCodeException(ErrorCode.AUTH_CODE_MISMATCH);
    // }
    //
    // if (userRepository.existsByEmail(email)) {
    // throw new MemberException(ErrorCode.EMAIL_ALREADY_EXISTS);
    // }
    //
    // String encodedPassword =
    // passwordEncoder.encode(signUpRequestDto.getProviderType() +
    // signUpRequestDto.getProviderId());
    //
    // User newUser = User.builder()
    // .email(email)
    // .password(encodedPassword)
    // .userRole(UserRole.USER)
    // .providerType(signUpRequestDto.getProviderType())
    // .providerId(signUpRequestDto.getProviderId())
    // .telecom(signUpRequestDto.getTelecom())
    // .gender(signUpRequestDto.getGender())
    //// .nickName(signUpRequestDto.getNickName())
    // .birthDate(signUpRequestDto.getBirthDate()) //
    //
    //
    // userRepository.save(newUser);
    // }
    // // 1. 이메일과 코드를 가지고 와서 비교하면서 코드가 틀리면 예외 발생. 이메일이 이미 존재하는 이메일일시 예외 밣생
    // // 2. 없으면 새로운 회원을 가입시킴
    //
    // public Map<String, String> login(String email, String snsType, String snsId)
    // {
    // User user = userRepository.findByEmail(email)
    // .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    //
    // if (!passwordEncoder.matches(snsType + snsId, user.getPassword())) {
    // throw new MemberException(ErrorCode.PASSWORD_NOT_MATCH);
    // }
    //
    // Authentication authentication = authenticationManager.authenticate(
    // new UsernamePasswordAuthenticationToken(email, snsType + snsId)
    // );
    // SecurityContextHolder.getContext().setAuthentication(authentication);
    //
    // String role = user.getUserRole().name();
    // String accessToken = jwtUtil.createAccessToken(user.getEmail(), role);
    // String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), role);
    // refreshRepository.saveRefreshToken(user.getEmail(), refreshToken);
    //
    //
    // Map<String, String> map = new HashMap<>();
    // map.put("accessToken", accessToken);
    // map.put("refreshToken", refreshToken);
    // return map;
    // }

    // public void logout(String accessToken) {
    //
    // String token = accessToken.replace("Bearer ", ""); // Bearer 제거
    //
    //
    // Claims claims;
    // try {
    // claims = jwtUtil.validateToken(token);
    // } catch (Exception e) {
    // throw new TokenException(ErrorCode.INVALID_ACCESS_TOKEN);
    // }
    //
    // String email = claims.getSubject();
    //
    // log.info("✅ 삭제 전 refresh token 조회: {}",
    // refreshRepository.findByEmail(email));
    //
    // refreshRepository.deleteRefreshToken(email);
    //
    // log.info("✅ 삭제 후 refresh token 조회: {}",
    // refreshRepository.findByEmail(email));
    //
    //
    // long tokenExpiration = jwtUtil.getTokenExpiration(token);
    // System.out.println("🔴 토큰 만료 시간(ms): " + tokenExpiration);
    //
    // if (tokenExpiration > 0) {
    // blackListRepository.addToBlackList(token, tokenExpiration);
    // log.info("🛑 블랙리스트 추가 완료: {}", token);
    // } else {
    // log.warn("⚠ 블랙리스트에 추가되지 않음: 만료 시간이 0 이하");
    // }
    // }

    @Transactional
    public void fcmTokenRefresh(User user, String fcmToken) {
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

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

    private String generateUniqueNickName() {
        String nickname;
        int attempt = 0;
        do {
            nickname = NickNameUtils.generateNickname();
            attempt++;
            if (attempt > 10) {
                throw new MemberException(ErrorCode.NICKNAME_GENERATION_FAILED);
            }
        } while (userRepository.existsByNickName(nickname));
        return nickname;
    }

    public Boolean hasNickName(User user) {
        return !(user.getNickName() == null || user.getNickName().equals(""));
    }

    public String limitNickname(String nickname) {
        String regex = "^[a-zA-Z0-9가-힣\\s]{2,8}$";
        if (!nickname.matches(regex)) {
            throw new ValidationException("올바르지 않은 형식의 이름입니다");
        } else {
            return "올바른 형식의 이름입니다";
        }
    }

    public void changeProfile(String email, String providerType, String providerId, String password) {
        User user = userRepository.findByEmail(email).orElseThrow();
        String encode = passwordEncoder.encode(password);
        user.setProviderType(providerType);
        user.setProviderId(providerId);
        user.setPassword(encode);
        userRepository.save(user);
    }

    public User modifyDescription(DescriptionRequestDto dto, User user) {
        user.setDescription(dto.getDescription());
        return userRepository.save(user);

    }

    public User changeProfileImage(User user, ProfileImageUpdateDto dto) {

        if (user.getImageUrl() != null && !user.getImageUrl().isBlank()) {
            String filename = user.getImageUrl().substring(user.getImageUrl().lastIndexOf("/") + 1);
            s3FileService.delete(filename);
        }

        if (dto.isResetToDefault()) {
            user.setImageUrl("avatar_placeholder.png");
            return userRepository.save(user);
        }

        MultipartFile profileImage = dto.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {

            String originalFilename = profileImage.getOriginalFilename();

            String uniqueName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
            s3FileService.upload(profileImage, uniqueName);
            String imageUrl = s3FileService.getFullUrl(uniqueName);
            user.setImageUrl(imageUrl);
        }
        return userRepository.save(user);
    }

    public User toggleAllAlarm(User user, boolean enabled) {

        user.setFeedAlarm(enabled);
        user.setFeedLikeAlarm(enabled);
        user.setSquadChatAlarm(enabled);
        user.setChatroomAlarm(enabled);
        user.setSquadNotifyAlarm(enabled);
        return userRepository.save(user);

    }

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

    public Map<String, String> getNickname(User user) {
        Map<String, String> map = new HashMap<>();
        map.put("nickname", user.getNickName() == null ? "" : user.getNickName());
        return map;
    }
}
