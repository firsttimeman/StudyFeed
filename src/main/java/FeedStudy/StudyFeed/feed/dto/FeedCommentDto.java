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

    public static FeedCommentDto toDto(FeedComment comment, Long userId){

        List<FeedCommentDto> allReplies = comment.getChildComments().stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt))
                .map(reply -> FeedCommentDto.toDto(reply, userId))
                .toList();

        List<FeedCommentDto> repliesShow = allReplies.size() > 2 ? allReplies.subList(0, 2) : allReplies;

        return new FeedCommentDto(
                comment.getId(),
                comment.getUser().getNickName(),
                comment.getUser().getImageUrl(),
                comment.getContent(),
                comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getUser().getId().equals(userId),
                repliesShow,
                allReplies.size() > 2);

    }

}
