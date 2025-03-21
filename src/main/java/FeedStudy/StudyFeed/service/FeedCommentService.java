package FeedStudy.StudyFeed.service;

import FeedStudy.StudyFeed.dto.FeedCommentRequestDto;
import FeedStudy.StudyFeed.entity.Feed.Feed;
import FeedStudy.StudyFeed.entity.Feed.FeedComment;
import FeedStudy.StudyFeed.entity.User;
import FeedStudy.StudyFeed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.repository.FeedRepository;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedCommentService {

    private final FeedCommentRepository commentRepository;
    private final FeedRepository feedRepository;

    public String insertFeedComment(User user, FeedCommentRequestDto req, FirebasePublisherService firebasePublisherService) {
        Feed foundFeedId = feedRepository.findById(req.getFeedId())
                .orElseThrow(() -> new RuntimeException("not found feedId"));
        FeedComment parent = null;
        if (req.getCommentId() != null) {
            parent = commentRepository.findById(req.getCommentId())
                    .orElseThrow(() -> new RuntimeException("not found comment Id"));
        }

        String comment = req.getComment();

        FeedComment newComment = FeedComment.builder()
                .comment(comment)
                .feed(foundFeedId)
                .parent(parent)
                .user(user)
                .build();

        commentRepository.save(newComment);


        User targetUser;
        String fcmToken;
        String title;
        String content = req.getComment();

        if (req.getCommentId() == null) {
            targetUser = foundFeedId.getUser();
            title = "작성하신 피드의 새로운 댓글 이에요.";
        } else {
            targetUser = parent.getUser();
            title = "작성하신 댓글의 새로운 답글이에요.";
        }

        fcmToken = targetUser.getFcmToken();

        if (targetUser.getReceiveFeedAlarm() && fcmToken != null) {
            try {
                return firebasePublisherService.postToClient(title, content, fcmToken);
            } catch (FirebaseMessagingException e) {
                throw new RuntimeException("푸시 알림 발송이 실패했습니다.", e);
            }
        }

        return null;
    }


}
