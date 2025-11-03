package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.global.type.Topic;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FeedDetailDto {
    private Long id;
    private String nickname;
    private String profileUrl;
    private Topic category;
    private String content;
    private List<String> imageList;
    private boolean likedByMe;
    private int likeCount;
    private int commentCount;
    private boolean mine;
    private String createdAt;

    private boolean hasMoreComments;
    private List<FeedCommentDto> comments;
}