package FeedStudy.StudyFeed.controller;

import FeedStudy.StudyFeed.dto.FeedEditRequest;
import FeedStudy.StudyFeed.dto.FeedReportRequest;
import FeedStudy.StudyFeed.entity.Feed.Feed;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.service.FeedLikeService;
import FeedStudy.StudyFeed.service.FeedReportService;
import FeedStudy.StudyFeed.service.FeedService;
import FeedStudy.StudyFeed.service.FirebasePublisherService;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
// @RequiredArgsConstructor
public class FeedController {
    @Autowired
    private FeedService feedService;
    @Autowired
    private FeedReportService feedReportService;
    @Autowired
    private FeedLikeService feedLikeService;

    private final FirebaseMessaging fcm;
    private FirebasePublisherService firebasePublisherService;

    public FeedController(FirebaseMessaging fcm) {
        this.fcm = fcm;
        firebasePublisherService = new FirebasePublisherService(fcm);
    }

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


    /**
     * 댓글 삭제하기
     */

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


}
