package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.FeedComment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedCommentDto {
    private Long id;
    private Long parentId;
    private String authorName;
    private String comment;
    private LocalDateTime createTime;

    public static FeedCommentDto fromEntity(FeedComment feedComment) {
        return new FeedCommentDto(
                feedComment.getId(),
                feedComment.getParent() != null ? feedComment.getParent().getId() : null,
                feedComment.getUser().getNickName(),
                feedComment.getComment(),
                feedComment.getCreateTime()
        );
    }

}
