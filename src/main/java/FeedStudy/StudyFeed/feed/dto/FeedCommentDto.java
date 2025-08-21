package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.FeedComment;
import FeedStudy.StudyFeed.global.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedCommentDto {
    private Long id;
    private String nickName;
    private String profileImageUrl;
    private String content;
    private String dateTime;
    private Long parentId;
    private boolean isMine;
    private List<FeedCommentDto> replies;
    private boolean hasMoreReplies;
    private boolean isDeleted;

    public static FeedCommentDto toDto(FeedComment comment, Long userId){

        List<FeedCommentDto> allReplies = comment.getChildComments().stream() // todo 여기서 n+1 문제 가능
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt))
                .map(reply -> FeedCommentDto.toDto(reply, userId))
                .toList();

        List<FeedCommentDto> repliesShow = allReplies.size() > 2 ? allReplies.subList(0, 2) : allReplies;

        boolean isDeleted = comment.isDeleted();
        String content = isDeleted ? "작성자가 댓글을 삭제했습니다." : comment.getContent();
        String nickName = isDeleted ? null : comment.getUser().getNickName(); // todo n+1 문제 가능
        String profileImageUrl = isDeleted ? null : comment.getUser().getImageUrl();  // todo n+1 문제 가능


        return new FeedCommentDto(
                comment.getId(),
                nickName,
                profileImageUrl,
                content,
                comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null, //  // todo n+1 문제 가능
                comment.getUser().getId().equals(userId),
                repliesShow,
                allReplies.size() > 2,
                isDeleted
        );
    }

    }


