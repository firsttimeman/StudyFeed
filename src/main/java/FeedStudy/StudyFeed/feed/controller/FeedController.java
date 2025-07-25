package FeedStudy.StudyFeed.feed.controller;

import FeedStudy.StudyFeed.feed.dto.*;
import FeedStudy.StudyFeed.feed.service.FeedService;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Feed api", description = "피드 api")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Slf4j
public class FeedController {
    private final FeedService feedService;



    /**
     * 등록
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 등록", description = "새로운 피드를 등록합니다.")
    public ResponseEntity<String> createFeed(@AuthenticationPrincipal User user, @Valid @ModelAttribute FeedRequest request) {
        feedService.create(user, request);
        return ResponseEntity.ok("Success");
    }



    @GetMapping("/{feedId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드를 가져옵니다.", description = "피드를 가져옵니다.")
    public ResponseEntity<FeedDetailResponse> getFeed(@AuthenticationPrincipal User user,
                                                            @Parameter(description = "피드 id") @PathVariable Long feedId) {
        FeedDetailResponse response = feedService.getFeed(user, feedId);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/comment/{commentId}/replies")
    @PreAuthorize("hasRole('USER')")
    @Operation(description = "댓글을 조회합니다.", summary = "댓글 조회")
    public ResponseEntity<?> getReplies(@AuthenticationPrincipal User user,
                                        @Parameter(description = "댓글 ID", example = "123")  @PathVariable Long commentId,
                                        @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC,
                                         size = 10) Pageable pageable) {
        FeedRepliesDto replies = feedService.getReplies(user, commentId, pageable);
        return ResponseEntity.ok(replies);
    }

    /**
     * 수정
     */
    @PutMapping("/modify/{feedId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 수정", description = "작성한 피드를 수정합니다.")
    public ResponseEntity<String> modifyFeed(@AuthenticationPrincipal User user,
                                             @Parameter(description = "피드 ID") @PathVariable Long feedId,
                                             @ModelAttribute FeedRequest request)  {
        feedService.update(user, request, feedId);
        return ResponseEntity.ok("Success modify");
    }




    /**
     * 삭제
     */
    @DeleteMapping("/delete/{feedId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 삭제", description = "작성한 피드를 삭제합니다.")
    public ResponseEntity<String> deleteFeed(@AuthenticationPrincipal User user,
                                             @Parameter(description = "피드 ID") @PathVariable Long feedId) {
        feedService.delete(user, feedId);
        return ResponseEntity.ok("Success delete");
    }

    @GetMapping("/like/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "피드 좋아요", description = "피드에 좋아요를 누릅니다.")
    public ResponseEntity<?> likeFeed(@AuthenticationPrincipal User user,
                                      @Parameter(description = "피드 ID")  @PathVariable Long id) {
        FeedLikeDto feedLikeDto = feedService.feedLike(user, id);
        return ResponseEntity.ok(feedLikeDto);
    }


    @GetMapping("/mine")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "내 피드 조회", description = "내가 작성한 피드를 조회합니다.")
    public ResponseEntity<?> mine(@AuthenticationPrincipal User user,
                                  @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10)
                                  Pageable pageable) {
        FeedResponseDto feedResponseDto = feedService.myFeeds(user.getId(), pageable);
        return ResponseEntity.ok(feedResponseDto);
    }




    /**
     * 홈피드
     * 모든 유저의 피드 + 차단된 유저는 제외
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/home")
    @Operation(summary = "홈 피드 조회", description = "모든 유저의 피드를 최신순으로 조회합니다. 차단된 유저는 제외됩니다.")
    public ResponseEntity<?> home(@AuthenticationPrincipal User user,
                                  @ParameterObject @PageableDefault(
                                           sort="createdAt",
                                           direction = Sort.Direction.DESC,
                                           size = 10
                                   ) Pageable pageable,
                                  @Parameter(description = "피드 카테고리") @RequestParam String category) {
        DataResponse homeFeeds = feedService.getHomeFeeds(user, pageable, category);
        return ResponseEntity.ok().body(homeFeeds);
    }


    /**
     * 내 피드
     * 카테고리 뺴기
     */
    @PostMapping("/others/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "타인의 피드 조회", description = "다른 사용자의 피드를 최신순으로 조회합니다.")
    public ResponseEntity<?> other(@AuthenticationPrincipal User user,
                                   @Parameter(description = "타인 사용자 ID") @PathVariable Long id,
                                   @ParameterObject
                                       @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable) {

        DataResponse dataResponse = feedService.otherFeeds(user.getId(), id, pageable);
        return ResponseEntity.ok(dataResponse);
    }


    /**
     * 댓글 달기
     */
    @PostMapping("/createcomment")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "댓글 작성", description = "피드에 댓글을 작성합니다.")
    public ResponseEntity<?> createComment(@AuthenticationPrincipal User user, @ModelAttribute FeedCommentRequestDto req) {
        feedService.writeComment(user, req);
        return ResponseEntity.ok().build();
    }



    @DeleteMapping("/deletecomment/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다.")
    public ResponseEntity<?> deleteComment(@AuthenticationPrincipal User user,
                                           @Parameter(description = "댓글 ID") @PathVariable Long id) {
        feedService.deleteComment(user.getId(), id);
        return ResponseEntity.ok().build();
    }





}
