package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedComment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FeedDetailResponse {

    private Long id;
    private String nickName;
    private String profileImageUrl;
    private String category;
    private String content;
    private List<String> images;
    private boolean like;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isMine;
    private String dateTime;
    private List<FeedCommentDto> comments;


    public static FeedDetailResponse toDto(Feed feed, Long userId, boolean isLike) {
        Long id = feed.getId();
        String nickName = feed.getUser().getNickName();
        String profileImageUrl = feed.getUser().getImageUrl();
        String category = feed.getCategory();
        String content = feed.getContent();
        List<String> images = feed.getImages().stream().map(image -> image.getImageUrl()).toList();
        Integer likeCount = feed.getLikeCount();
        Integer commentCount = feed.getCommentCount();
        Boolean isMine = feed.getUser().getId() == userId;
        String dateTime = feed.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<FeedCommentDto> comments = feed.getComments().stream()
                .map(comment -> FeedCommentDto.toDto(comment, userId)).toList();

        return new FeedDetailResponse(id, nickName, profileImageUrl, category, content, images, isLike,
                likeCount, commentCount, isMine, dateTime, comments);
    }
}
