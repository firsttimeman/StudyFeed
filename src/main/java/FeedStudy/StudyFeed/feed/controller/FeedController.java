package FeedStudy.StudyFeed.feed.controller;

import FeedStudy.StudyFeed.feed.dto.FeedCommentDto;
import FeedStudy.StudyFeed.feed.dto.FeedCommentRequestDto;
import FeedStudy.StudyFeed.feed.dto.FeedEditRequest;
import FeedStudy.StudyFeed.feed.dto.FeedReportRequest;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.service.FeedCommentService;
import FeedStudy.StudyFeed.feed.service.FeedLikeService;
import FeedStudy.StudyFeed.feed.service.FeedReportService;
import FeedStudy.StudyFeed.feed.service.FeedService;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;
    private final FeedReportService feedReportService;
    private final FeedLikeService feedLikeService;
    private final FeedCommentService feedCommentService;


    /**
     * 등록
     */
    @PostMapping
    public ResponseEntity<String> createFeed(@AuthenticationPrincipal User user, @RequestBody FeedEditRequest request) {
        feedService.create(user, request);
        return ResponseEntity.ok("Success");
    }


    /**
     * 수정
     */
    @PutMapping("/{feedId}")
    public ResponseEntity<String> modifyFeed(@AuthenticationPrincipal User user,
                                             @PathVariable Long feedId,
                                             @RequestBody FeedEditRequest request) {
        feedService.modify(user, request, feedId);
        return ResponseEntity.ok("Success modify");
    }




    /**
     * 삭제
     */
    @DeleteMapping("/{feedId}")
    public ResponseEntity<String> deleteFeed(@AuthenticationPrincipal User user, @PathVariable Long feedId) {
        feedService.delete(user, feedId);
        return ResponseEntity.ok("Success delete");
    }


    /**
     * 홈피드
     * 모든 유저의 피드 + 차단된 유저는 제외
     */
    @GetMapping("/home")
    public Page<Feed> getHomeFeeds(@AuthenticationPrincipal User user, Pageable pageable) {
        return feedService.getHomeFeeds(user, pageable);
    }


    /**
     * 내 피드
     * 카테고리 뺴기
     */
    @GetMapping
    public Page<Feed> getAllFeed(@AuthenticationPrincipal User user, Pageable pageable) {

        return feedService.getMyFeeds(user, pageable);
    }


    /**
     * 상대방 피드
     */
    @GetMapping("/user/{userId}")
    public Page<Feed> getUserFeeds(@AuthenticationPrincipal User user, @PathVariable Long userId, Pageable pageable) {
        return feedService.getUserFeeds(user, userId, pageable);
    }

    /**
     * 댓글 달기
     */
    @PostMapping("/AddComment")
    public ResponseEntity<?> addComment(@AuthenticationPrincipal User user,
                                        @Valid @RequestBody FeedCommentRequestDto dto) {
        String result = feedCommentService.insertFeedComment(user, dto);
        if(result != null) {
            return ResponseEntity.ok("댓글 작성 + 알림 전송 완료");
        } else {
            return ResponseEntity.ok("댓글 작성 완료 (알림 실패)");
        }

    }


    /**
     * 댓글 삭제하기
     */
    @DeleteMapping("/comment/{commendId}")
    public ResponseEntity<String> deleteComment(@AuthenticationPrincipal User user, @PathVariable Long commendId) {
        feedCommentService.deleteFeedComment(user, commendId);
        return ResponseEntity.ok("Success delete");
    }



    /**
     * 피드 좋아요
     */
    @PostMapping("/{feedId}")
    public ResponseEntity<String> likeClick(@AuthenticationPrincipal User user,
                                            @PathVariable Long feedId) {
        boolean isLiked = feedLikeService.likeClick(user, feedId);

        if (isLiked) {
            return ResponseEntity.ok("좋아요 완료");
        } else {
            return ResponseEntity.ok("좋아요 취소 완료");
        }
    }


    /**
     * 피드 신고하기
     */
    @PostMapping("/report/{feedId}")
    public ResponseEntity<String> reportFeed(@AuthenticationPrincipal User user,
                                             @PathVariable Long feedId,
                                             @Valid @RequestBody FeedReportRequest request
    ) {
        feedReportService.reportFeed(user, feedId, request.getReason());
        return ResponseEntity.ok("Success report");
    }

    @GetMapping("/{feedId}/comments/{commentId}/replies")
    public ResponseEntity<Page<FeedCommentDto>> getReplies(@PathVariable Long feedId,
                                                           @PathVariable("commentId") Long parentId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "2") int size) {
        Page<FeedCommentDto> replies = feedCommentService.getReplies(parentId, page, size);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/{feedId}/comments/{commentId}/replies/all")
    public ResponseEntity<List<FeedCommentDto>> getRepliesAll(@PathVariable Long feedId, @PathVariable("commentId") Long parentId) {
        List<FeedCommentDto> allReplies = feedCommentService.getAllReplies(parentId);
        return ResponseEntity.ok(allReplies);
    }


}
