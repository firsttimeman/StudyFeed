package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FeedSimpleDto {
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private String category;
    private String content;
    private List<String> images;
    private boolean like;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isMine;
    private String datetime;

    public static FeedSimpleDto toDto(Feed feed, Long userId, boolean isLike) {
        Long id = feed.getId();
        String nickname = feed.getUser().getNickName();
        String profileImageUrl = "";
        String category = feed.getCategory();
        String content = feed.getContent();
        List<String> images = feed.getImages().stream().map(i -> i.getImageUrl()).toList();
        Integer likeCount = feed.getLikeCount();
        Integer commentCount = feed.getCommentCount();
        Boolean isMine = feed.getUser().getId() == userId;
        String datetime = feed.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return new FeedSimpleDto(id, nickname, profileImageUrl, category, content,
                images, isLike, likeCount, commentCount, isMine, datetime);
    }
}
