package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.feed.dto.FeedCommentDto;
import FeedStudy.StudyFeed.feed.dto.FeedCommentRequestDto;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
import FeedStudy.StudyFeed.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedCommentService {
    private final FeedCommentRepository commentRepository;
    private final FeedRepository feedRepository;
    private final FirebaseMessagingService firebaseMessagingService;

    public String insertFeedComment(User user, FeedCommentRequestDto req) {
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
        String title;

        if (req.getCommentId() == null) {
            targetUser = foundFeedId.getUser();
            title = "작성하신 피드의 새로운 댓글 이에요.";
        } else {
            targetUser = parent.getUser();
            title = "작성하신 댓글의 새로운 답글이에요.";
        }

     return firebaseMessagingService.sendCommentNotification(targetUser, title, req.getComment());
    }

    @Transactional
    public void deleteFeedComment(User user, Long commentId) {

        FeedComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("not found comment Id"));

        if(!comment.getUser().equals(user)) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);

    }

    public Page<FeedCommentDto> getReplies(Long parentId, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createTime").ascending()
        );
        return commentRepository.findByParentId(parentId, pageable)
                .map(FeedCommentDto::fromEntity);
    }

    public List<FeedCommentDto> getAllReplies(Long parentId) {
        return commentRepository.findByParentId(parentId, Sort.by("createTime").ascending())
                .stream()
                .map(feedComment -> FeedCommentDto.fromEntity(feedComment))
                .toList();
    }



}
