package FeedStudy.StudyFeed.feed.controller;

import FeedStudy.StudyFeed.feed.dto.*;
import FeedStudy.StudyFeed.feed.service.FeedService;
import FeedStudy.StudyFeed.global.dto.DataResponse;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> createFeed(@AuthenticationPrincipal User user, @ModelAttribute FeedRequest request) {
        feedService.create(user, request);
        return ResponseEntity.ok("Success");
    }



    @GetMapping("/{feedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FeedDetailResponse> getFeed(@AuthenticationPrincipal User user,
                                                            @PathVariable Long feedId) {
        FeedDetailResponse response = feedService.getFeed(user, feedId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comment/{commentId}/replies")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getReplies(@AuthenticationPrincipal User user,
                                        @PathVariable Long commentId,
                                        @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC,
                                         size = 10) Pageable pageable) {
        FeedRepliesDto replies = feedService.getReplies(user, commentId, pageable);
        return ResponseEntity.ok(replies);
    }

    /**
     * 수정
     */
    @PutMapping("/modify/{feedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> modifyFeed(@AuthenticationPrincipal User user,
                                             @PathVariable Long feedId,
                                             @ModelAttribute FeedRequest request)  {
        feedService.update(user, request, feedId);
        return ResponseEntity.ok("Success modify");
    }




    /**
     * 삭제
     */
    @DeleteMapping("/delete/{feedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteFeed(@AuthenticationPrincipal User user, @PathVariable Long feedId) {
        feedService.delete(user, feedId);
        return ResponseEntity.ok("Success delete");
    }

    @GetMapping("/like/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> likeFeed(@AuthenticationPrincipal User user, @PathVariable Long id) {
        FeedLikeDto feedLikeDto = feedService.feedLike(user, id);
        return ResponseEntity.ok(feedLikeDto);
    }


    @GetMapping("/mine")
    public ResponseEntity<?> mine(@AuthenticationPrincipal User user,
                                  @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10)
                                  Pageable pageable) {
        FeedResponseDto feedResponseDto = feedService.myFeeds(user.getId(), pageable);
        return ResponseEntity.ok(feedResponseDto);
    }




    /**
     * 홈피드
     * 모든 유저의 피드 + 차단된 유저는 제외
     */
    @GetMapping("/home")
    public ResponseEntity<?> home(@AuthenticationPrincipal User user,
                                     @PageableDefault(
                                           sort="createdAt",
                                           direction = Sort.Direction.DESC,
                                           size = 10
                                   ) Pageable pageable,
                             @RequestParam String category) {
        DataResponse homeFeeds = feedService.getHomeFeeds(user, pageable, category);
        return ResponseEntity.ok().body(homeFeeds);
    }


    /**
     * 내 피드
     * 카테고리 뺴기
     */
    @PostMapping("/others/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> other(@AuthenticationPrincipal User user, @PathVariable Long id,
                                   @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable) {

        DataResponse dataResponse = feedService.otherFeeds(user.getId(), id, pageable);
        return ResponseEntity.ok(dataResponse);
    }


    /**
     * 댓글 달기
     */
    @PostMapping("/createcomment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createComment(@AuthenticationPrincipal User user, @ModelAttribute FeedCommentRequestDto req) {
        feedService.writeComment(user, req);
        return ResponseEntity.ok().build();
    }



    @DeleteMapping("/deletecomment/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteComment(@AuthenticationPrincipal User user, @PathVariable Long id) {
        feedService.deleteComment(user.getId(), id);
        return ResponseEntity.ok().build();
    }





}
