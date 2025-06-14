package FeedStudy.StudyFeed.feed.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FeedCommentRequestDto {

    private Long feedCommentPid;

    @NotNull
    private String content;

    @NotNull
    private Long feedPid;


}
