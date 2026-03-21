package FeedStudy.StudyFeed.feed.controller;

import FeedStudy.StudyFeed.feed.dto.*;
import FeedStudy.StudyFeed.feed.service.FeedCommentService;
import FeedStudy.StudyFeed.feed.service.FeedLikeService;
import FeedStudy.StudyFeed.feed.service.FeedService;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Feed API", description = "피드 관련 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Slf4j
public class FeedController {

    private final FeedService feedService;
    private final FeedLikeService feedLikeService;
    private final FeedCommentService feedCommentService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 이미지 업로드", description = "이미지를 S3에 업로드하고 URL 리스트를 반환합니다.")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<String> imageUrls = feedService.uploadImagesInS3(user.getId(), files);
        return ResponseEntity.ok(new ImageUploadResponse(imageUrls));
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 등록", description = "새로운 피드를 등록합니다. 이미지 URL은 /images 업로드 후 받은 값을 사용합니다.")
    public ResponseEntity<FeedDetailResponse> createFeed(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody FeedRequest request
    ) {
        FeedDetailResponse response = feedService.create(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{feedId}/detail")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 상세 조회", description = "피드 상세정보 + 댓글 미리보기")
    public ResponseEntity<FeedDetailDto> getFeedDetail(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long feedId,
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 10)
            Pageable pageable,
            @RequestParam(defaultValue = "2") int previewLimit
    ) {
        FeedDetailDto dto = feedService.getFeedDetail(user, feedId, pageable, previewLimit);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/comments/{commentId}/replies")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "대댓글 페이지 조회")
    public ResponseEntity<FeedRepliesDto> getReplies(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long commentId,
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC, size = 10)
            Pageable pageable
    ) {
        return ResponseEntity.ok(feedCommentService.getReplies(user, commentId, pageable));
    }

    @PatchMapping(value = "/{feedId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 수정", description = "본문/카테고리 및 이미지 URL 추가/삭제")
    public ResponseEntity<Void> modifyFeed(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long feedId,
            @Valid @RequestBody FeedRequest request
    ) {
        feedService.update(user, request, feedId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{feedId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 삭제")
    public ResponseEntity<Void> deleteFeed(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long feedId
    ) {
        feedService.delete(user, feedId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{feedId}/like")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 좋아요 토글")
    public ResponseEntity<FeedLikeDto> likeFeed(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long feedId
    ) {
        return ResponseEntity.ok(feedLikeService.toggleLike(user, feedId));
    }

    @GetMapping("/home")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "홈 피드 조회")
    public ResponseEntity<DataResponse> home(
            @AuthenticationPrincipal(expression = "user") User user,
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 10)
            Pageable pageable,
            @RequestParam(required = false) Topic category
    ) {
        return ResponseEntity.ok(feedService.getHomeFeeds(user, pageable, category));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "내 피드 조회")
    public ResponseEntity<DataResponse> myFeeds(
            @AuthenticationPrincipal(expression = "user") User user,
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 10)
            Pageable pageable
    ) {
        return ResponseEntity.ok(feedService.getMyFeeds(user, pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "타인 피드 조회")
    public ResponseEntity<DataResponse> userFeeds(
            @AuthenticationPrincipal(expression = "user") User me,
            @PathVariable Long userId,
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 10)
            Pageable pageable
    ) {
        return ResponseEntity.ok(feedService.getUserFeeds(me, userId, pageable));
    }

    @PostMapping(value = "/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "댓글 작성")
    public ResponseEntity<Void> createComment(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody FeedCommentRequestDto req
    ) {
        feedCommentService.writeComment(user, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "댓글 삭제")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long id
    ) {
        feedCommentService.deleteComment(user.getId(), id);
        return ResponseEntity.ok().build();
    }
}