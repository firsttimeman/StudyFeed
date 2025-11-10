package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.global.type.Topic;
import FeedStudy.StudyFeed.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FeedSimpleDto {
    private Long id;
    private String nickname;
    private String profileUrl;
    private Topic category;
    private String content;
    private List<String> imageList;
    private boolean like;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean mine;
    private String regDt;


    public static FeedSimpleDto of(Feed feed,
                                   List<String> imageUrls,
                                   boolean likedByMe,
                                   Long currentUserId) {

        boolean mine = feed.getUser().getId().equals(currentUserId);
        String datetime = (feed.getCreatedAt() != null)
                ? feed.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return new FeedSimpleDto(
                feed.getId(),
                feed.getUser().getNickName(),
                feed.getUser().getImageUrl(),
                feed.getCategory(),
                feed.getContent(),
                imageUrls,
                likedByMe,
                feed.getLikeCount(),
                feed.getCommentCount(),
                mine,
                datetime
        );
    }


}
