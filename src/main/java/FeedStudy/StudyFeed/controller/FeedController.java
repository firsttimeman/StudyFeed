package FeedStudy.StudyFeed.controller;

import FeedStudy.StudyFeed.dto.FeedEditRequest;
import FeedStudy.StudyFeed.dto.FeedReportRequest;
import FeedStudy.StudyFeed.entity.Feed;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.service.FeedLikeService;
import FeedStudy.StudyFeed.service.FeedReportService;
import FeedStudy.StudyFeed.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;
    private final FeedReportService feedReportService;
    private final FeedLikeService feedLikeService;

    /**
     * 등록
     */
    @PostMapping
    public ResponseEntity<String> createFeed(@AuthenticationPrincipal User user, @RequestBody FeedEditRequest request) {
        feedService.create(user, request);
        return ResponseEntity.ok("Success");
    }

    /**
     * 추가
     */


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
     */
    @GetMapping
    public Page<Feed> getAllFeed(@AuthenticationPrincipal User user,
                                 @RequestParam(required = false) String category,
                                 Pageable pageable
                                 ) {

        return feedService.getMyFeeds(user, category, pageable);
    }


    /**
     * 내 피드
     */



    /**
     * 상대방 피드
     * //TODO 이거 물어보기
     */
    @GetMapping("/user/{userId}")
    public Page<Feed> getUserFeeds(@AuthenticationPrincipal User user, @PathVariable Long userId, Pageable pageable) {
        return feedService.getUserFeeds(user, userId, pageable);
    }

    /**
     * 댓글 달기
     */


    /**
     * 댓글 삭제하기
     */

    /**
     * 피드 좋아요
     */
    @PostMapping("/{feedId}")
    public ResponseEntity<String> likeClick(@AuthenticationPrincipal User user,
                                            @PathVariable Long feedId) {
        boolean isLiked = feedLikeService.LikeClick(user, feedId);

        if (isLiked) {
            return ResponseEntity.ok("좋아요 완료");
        } else {
            return ResponseEntity.ok("좋아요 취소 완료");
        }
    }



    /**
     * 피드 신고하기
     */
    @PostMapping("/{feedId}")
    public ResponseEntity<String> reportFeed(@AuthenticationPrincipal User user,
                                             @PathVariable Long feedId,
                                             @Valid @RequestBody FeedReportRequest request
    ) {
        feedReportService.reportFeed(user, feedId, request.getReason());
        return ResponseEntity.ok("Success report");
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<String> unReportFeed(@AuthenticationPrincipal User user,
                                               @PathVariable Long feedId) {
        feedReportService.unReportFeed(user, feedId);
        return ResponseEntity.ok("신고 취소 완료");
    }



}
