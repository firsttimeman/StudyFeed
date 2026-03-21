package FeedStudy.StudyFeed.feed.service;

import FeedStudy.StudyFeed.block.repository.BlockRepository;
import FeedStudy.StudyFeed.feed.dto.FeedCommentDto;
import FeedStudy.StudyFeed.feed.dto.FeedCommentRequestDto;
import FeedStudy.StudyFeed.feed.dto.FeedRepliesDto;
import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.feed.repository.FeedCommentRepository;
import FeedStudy.StudyFeed.feed.repository.FeedRepository;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.FeedException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedCommentService {

    private final FeedRepository feedRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final BlockRepository blockRepository;

    @Transactional
    public void writeComment(User user, FeedCommentRequestDto dto) {
        Feed feed = feedRepository.findById(dto.getFeedId())
                .orElseThrow(() -> new FeedException(ErrorCode.FEED_NOT_FOUND));

        validateViewPermission(user, feed);


        String raw = Optional.ofNullable(dto.getContent()).orElse("").trim();
        if (raw.isEmpty() || raw.length() > 1000) {
            throw new FeedException(ErrorCode.INVALID_CONTENT);
        }

        FeedComment parent = null;
        if(dto.getParentCommentId() != null) {
            parent = feedCommentRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

            if(!parent.getFeed().getId().equals(feed.getId())) {
                throw new FeedException(ErrorCode.INVALID_PARENT_COMMENT);
            }


            if (parent.isDeleted()) {
                throw new FeedException(ErrorCode.COMMENT_DELETED_CANNOT_REPLY);
            }


            if ((parent.getUser() != null && blockRepository.existsByBlockerAndBlocked(user, parent.getUser()))
                || (parent.getUser() != null && blockRepository.existsByBlockerAndBlocked(parent.getUser(), user))) {
                throw new FeedException(ErrorCode.BANNED_USER);
            }
        }

        feedCommentRepository.save(new FeedComment(user, feed, raw, parent));

        feedRepository.increaseCommentCount(feed.getId());

        if (parent != null) {
            feedCommentRepository.increaseReplyCount(parent.getId());
        }


    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        FeedComment comment = feedCommentRepository.findByIdWithFeedAndParentAndUser(commentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getUser() == null || !comment.getUser().getId().equals(userId)) {
            throw new FeedException(ErrorCode.NOT_COMMENT_OWNER);
        }

        boolean hasReplies = comment.getReplyCount() > 0;

        if (hasReplies) {
            comment.markAsDeleted();
            return;
        }

        if (comment.getParentComment() != null) {
            feedCommentRepository.decreaseReplyCount(comment.getParentComment().getId());
        }

        feedRepository.decreaseCommentCount(comment.getFeed().getId());
        feedCommentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public FeedRepliesDto getReplies(User me, Long parentId, Pageable pageable) {
        FeedComment parent = feedCommentRepository.findById(parentId)
                .orElseThrow(() -> new FeedException(ErrorCode.COMMENT_NOT_FOUND));

        validateViewPermission(me, parent.getFeed());

        Page<FeedComment> page = feedCommentRepository.findReplies(parentId, pageable);

        var replies = page.getContent().stream()
                .map(reply -> FeedCommentDto.forReply(reply, parentId, me.getId()))
                .toList();

        int fetched = page.getPageable().getPageNumber() * page.getSize() + page.getNumberOfElements();
        boolean hasMore = parent.getReplyCount() > fetched;

        return new FeedRepliesDto(hasMore, replies);
    }


    private void validateViewPermission(User currentUser, Feed feed) {
        User feedOwner = feed.getUser();

        boolean isBlocked = blockRepository.existsByBlockerAndBlocked(currentUser, feedOwner)
                            || blockRepository.existsByBlockerAndBlocked(feedOwner, currentUser);

        if (isBlocked) {
            throw new MemberException(ErrorCode.BANNED_USER);
        }
    }





}
