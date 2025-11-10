package FeedStudy.StudyFeed.user.service;


import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.global.utils.NickNameUtils;

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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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




    public String makeNickName(User user) {

        String generateNickName = generateUniqueNickName();
        user.setNickName(generateNickName);
        userRepository.save(user);
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

        String oldUrl = user.getImageUrl(); // 나중에 삭제할 후보

        // 1) 기본 이미지로 되돌리기
        if (dto.isResetToDefault()) {

            user.setImageUrl(DEFAULT_IMAGE);
            User saved = userRepository.save(user);

            // 예전 이미지가 있고, 기본 이미지가 아니라면 → 커밋 이후 S3에서 삭제
            if (oldUrl != null && !oldUrl.isBlank() && !oldUrl.endsWith(DEFAULT_IMAGE)
                && TransactionSynchronizationManager.isSynchronizationActive()) {

                String oldKey = s3FileService.extractKeyFromUrl(oldUrl);

                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            s3FileService.delete(oldKey);
                        } catch (Exception e) {
                            log.warn("기존 프로필 이미지 삭제 실패: {}", oldKey, e);
                        }
                    }
                });
            }

            return saved;
        }

        // 2) 새 이미지 업로드
        MultipartFile file = dto.getProfileImage();
        if (file == null || file.isEmpty()) {
            // 업로드할 파일이 없으면 그냥 현재 유저 상태 반환
            return user;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("JPEG/PNG/GIF/WEBP 형식의 이미지 파일만 업로드할 수 있어요.");
        }

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains(".")) ?
                original.substring(original.lastIndexOf('.')) : "";

        // 👉 프로필 이미지는 따로 prefix를 두는 게 관리 편함
        String newKey = String.format("profile/%d/%s%s", user.getId(), UUID.randomUUID(), ext);

        // 새 이미지 S3 업로드 (여기는 어쩔 수 없이 업로드가 끝날 때까지는 기다려야 함)
        String newUrl = s3FileService.uploadAndReturnUrl(file, newKey);

        user.setImageUrl(newUrl);
        User saved = userRepository.save(user);

        // 3) 예전 이미지 S3 삭제는 afterCommit으로 (베스트 에포트)
        if (oldUrl != null && !oldUrl.isBlank() && !oldUrl.endsWith(DEFAULT_IMAGE)
            && TransactionSynchronizationManager.isSynchronizationActive()) {

            String oldKey = s3FileService.extractKeyFromUrl(oldUrl);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        s3FileService.delete(oldKey);
                    } catch (Exception e) {
                        log.warn("기존 프로필 이미지 삭제 실패: {}", oldKey, e);
                    }
                }
            });
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

    //todo 나중에 채팅 기능 향방보고 결정
    @Transactional
    public void deleteUser(User user) {
        Long uid = user.getId();

        if (!userRepository.existsById(uid)) {
            throw new MemberException(ErrorCode.USER_NOT_FOUND);
        }

        // --- 1️⃣ S3 삭제 리스트 ---
        List<String> s3Keys = new ArrayList<>();

        // 프로필 이미지
        if (user.getImageUrl() != null && !user.getImageUrl().isBlank()
            && !user.getImageUrl().endsWith("avatar_placeholder.png")) {
            s3Keys.add(s3FileService.extractKeyFromUrl(user.getImageUrl()));
        }

        // --- 2️⃣ Feed (내 피드글은 하드 삭제) ---
        List<Long> feedIds = feedRepository.findIdsByOwner(uid);
        if (!feedIds.isEmpty()) {
            List<String> feedImageUrls = feedImageRepository.findUrlsByFeedIds(feedIds);
            feedImageUrls.stream()
                    .map(s3FileService::extractKeyFromUrl)
                    .forEach(s3Keys::add);

            feedRepository.deleteAllByOwner(uid);
        }

        // --- 3️⃣ FeedComment (타인의 피드에 단 댓글은 소프트 삭제) ---
        feedCommentRepository.softDeleteOthersByUser(uid);

        // --- 4️⃣ FeedLike (좋아요 전부 제거) ---
        feedLikeRepository.deleteAllByUserId(uid);

        // --- 5️⃣ Squad (내가 개설한 모임은 하드 삭제) ---
        List<Long> squadIds = squadRepository.findIdsByOwner(uid);
        if (!squadIds.isEmpty()) {
            squadRepository.deleteAllByOwner(uid);
        }

        // --- 6️⃣ SquadChat (내가 쓴 채팅은 소프트 삭제 + 이미지 삭제) ---
        List<String> chatImageKeys = squadChatRepository.findAllImageKeysByAuthor(uid);
        if (!chatImageKeys.isEmpty()) {
            chatImageKeys.forEach(s3Keys::add);
            squadChatRepository.deleteAllImagesByAuthor(uid);
        }
        squadChatRepository.softDeleteAllByAuthor(uid);

        // --- 7️⃣ SquadMember (내 참여기록 삭제 + 정원 보정) ---
        List<Long> joinedSquads = squadMemberRepository.findJoinedSquadIds(uid);
        for (Long squadId : joinedSquads) {
            squadRepository.tryDecreaseCount(squadId);
            squadRepository.openIfNotFull(squadId);
        }
        squadMemberRepository.deleteAllJoined(uid);
        squadMemberRepository.deleteAllPending(uid);
        squadMemberRepository.cleanupNonJoined(uid);

        // --- 8️⃣ RefreshToken 제거 ---
        refreshRepository.deleteRefreshToken(user.getEmail());
        user.setFcmToken(null);

        // --- 9️⃣ 최종 유저 삭제 ---
        userRepository.delete(user);

        // --- 🔟 트랜잭션 커밋 후 S3 삭제 ---
        if (!s3Keys.isEmpty() && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (String key : s3Keys) {
                        try {
                            s3FileService.delete(key);
                        } catch (Exception e) {
                            log.warn("S3 파일 삭제 실패: {}", key, e);
                        }
                    }
                }
            });
        }

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
