package FeedStudy.StudyFeed.user.service;

import FeedStudy.StudyFeed.auth.repository.RefreshRepository;
import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedImageRepository;
import FeedStudy.StudyFeed.feed.repository.FeedLikeRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.service.S3FileService;
import FeedStudy.StudyFeed.global.utils.NickNameUtils;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.dto.DescriptionRequestDto;
import FeedStudy.StudyFeed.user.dto.NickNameCheckResponse;
import FeedStudy.StudyFeed.user.dto.ProfileImageUpdateDto;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshRepository refreshRepository;
    private final S3FileService s3FileService;

    private final FeedRepository feedRepository;
    private final FeedImageRepository feedImageRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final SquadMemberRepository squadMemberRepository;
    private final SquadRepository squadRepository;

    @Transactional
    public String makeNickName(User user) {
        String generatedNickName = generateUniqueNickName();
        user.changeNickName(generatedNickName);
        return generatedNickName;
    }

    @Transactional
    public void updateNickname(User user, String nickName) {
        String normalized = nickName == null ? "" : nickName.trim().replaceAll("\\s{2,}", " ");

        if (userRepository.existsByIdNotAndNickName(user.getId(), normalized)) {
            throw new MemberException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.changeNickName(normalized);
    }

    public NickNameCheckResponse checkNickname(String rawNickname) {
        String normalized = rawNickname == null ? "" : rawNickname.trim().replaceAll("\\s{2,}", " ");

        String regex = "^[a-zA-Z0-9가-힣\\s]{2,12}$";
        if (!normalized.matches(regex)) {
            return NickNameCheckResponse.builder()
                    .valid(false)
                    .available(false)
                    .message("2~12자, 한글/영문/숫자/공백만 사용할 수 있어요.")
                    .normalized(normalized)
                    .build();
        }

        if (normalized.matches("^[0-9]+$")) {
            return NickNameCheckResponse.builder()
                    .valid(false)
                    .available(false)
                    .message("숫자만으로는 닉네임을 만들 수 없어요.")
                    .normalized(normalized)
                    .build();
        }

        if (normalized.matches("^(.)\\1{3,}$")) {
            return NickNameCheckResponse.builder()
                    .valid(false)
                    .available(false)
                    .message("같은 문자를 4번 이상 반복할 수 없어요.")
                    .normalized(normalized)
                    .build();
        }

        boolean exists = userRepository.existsByNickName(normalized);
        if (exists) {
            return NickNameCheckResponse.builder()
                    .valid(true)
                    .available(false)
                    .message("이미 사용 중인 닉네임이에요.")
                    .normalized(normalized)
                    .build();
        }

        return NickNameCheckResponse.builder()
                .valid(true)
                .available(true)
                .message("사용 가능한 닉네임이에요!")
                .normalized(normalized)
                .build();
    }

    public boolean hasNickName(User user) {
        return user.getNickName() != null && !user.getNickName().isBlank();
    }

    public Map<String, String> getNickname(User user) {
        return Map.of("nickname", user.getNickName() == null ? "" : user.getNickName());
    }

    @Transactional
    public User modifyDescription(DescriptionRequestDto dto, User user) {
        user.changeDescription(dto.getDescription());
        return user;
    }

    @Transactional
    public User changeProfileImage(User user, ProfileImageUpdateDto dto) {
        final String DEFAULT_IMAGE = "avatar_placeholder.png";
        final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

        String oldUrl = user.getImageUrl();

        if (dto.isResetToDefault()) {
            user.changeImageUrl(DEFAULT_IMAGE);
            deleteOldProfileImageAfterCommit(oldUrl, DEFAULT_IMAGE);
            return user;
        }

        MultipartFile file = dto.getProfileImage();
        if (file == null || file.isEmpty()) {
            return user;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType.toLowerCase())) {
            throw new MemberException(ErrorCode.IMAGE_EXT_NOT_SUPPORTED);
        }

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : "";

        String newKey = String.format("profile/%d/%s%s", user.getId(), UUID.randomUUID(), ext);
        String newUrl = s3FileService.uploadAndReturnUrl(file, newKey);

        user.changeImageUrl(newUrl);
        deleteOldProfileImageAfterCommit(oldUrl, DEFAULT_IMAGE);

        return user;
    }

    private void deleteOldProfileImageAfterCommit(String oldUrl, String defaultImage) {
        if (oldUrl == null || oldUrl.isBlank() || oldUrl.endsWith(defaultImage)) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

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

    @Transactional
    public void deleteUser(User user) {
        Long uid = user.getId();

        if (!userRepository.existsById(uid)) {
            throw new MemberException(ErrorCode.USER_NOT_FOUND);
        }

        Set<String> s3Keys = new LinkedHashSet<>();

        if (isCustomProfileImage(user.getImageUrl())) {
            s3Keys.add(s3FileService.extractKeyFromUrl(user.getImageUrl()));
        }

        List<Long> feedIds = feedRepository.findIdsByOwner(uid);
        if (!feedIds.isEmpty()) {
            List<String> feedImageUrls = feedImageRepository.findUrlsByFeedIds(feedIds);
            feedImageUrls.stream()
                    .map(s3FileService::extractKeyFromUrl)
                    .forEach(s3Keys::add);

            feedRepository.deleteAllByOwner(uid);
        }

        feedCommentRepository.softDeleteOthersByUser(uid);
        feedLikeRepository.deleteAllByUserId(uid);

        List<Long> ownerSquadIds = squadRepository.findIdsByOwner(uid);
        if (!ownerSquadIds.isEmpty()) {
            squadRepository.deleteAllByOwner(uid);
        }

        Set<Long> ownerSquadIdSet = new HashSet<>(ownerSquadIds);

        List<Long> joinedSquads = squadMemberRepository.findJoinedSquadIds(uid).stream()
                .filter(id -> !ownerSquadIdSet.contains(id))
                .toList();

        for (Long squadId : joinedSquads) {
            squadRepository.tryDecreaseCount(squadId);
            squadRepository.openIfNotFull(squadId);
        }

        squadMemberRepository.deleteAllJoined(uid);
        squadMemberRepository.deleteAllPending(uid);
        squadMemberRepository.cleanupNonJoined(uid);

        refreshRepository.deleteRefreshToken(user.getEmail());
        userRepository.delete(user);

        deleteS3KeysAfterCommit(s3Keys);
    }

    private boolean isCustomProfileImage(String imageUrl) {
        return imageUrl != null
               && !imageUrl.isBlank()
               && !imageUrl.endsWith("avatar_placeholder.png");
    }

    private void deleteS3KeysAfterCommit(Collection<String> keys) {
        if (keys.isEmpty() || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String key : keys) {
                    try {
                        s3FileService.delete(key);
                    } catch (Exception e) {
                        log.warn("S3 파일 삭제 실패: {}", key, e);
                    }
                }
            }
        });
    }
    private String generateUniqueNickName() {
        for (int i = 0; i < 100; i++) {
            String nickname = NickNameUtils.generateNickname();
            if (!userRepository.existsByNickName(nickname)) {
                return nickname;
            }
        }

        throw new MemberException(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }
}