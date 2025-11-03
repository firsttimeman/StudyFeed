package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import FeedStudy.StudyFeed.global.type.Topic;
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
    private Topic category;
    private String content;
    private List<String> images;
    private boolean likedByMe; // 내가 이 피드에 좋아요를 눌렀는가?
    private Integer likeCount;
    private Integer commentCount;
    private boolean mine; // 내가 쓴 글인가?
    private String dateTime;


    public static FeedDetailResponse toDto(Feed feed, Long userId, boolean likedByMe) {
        Long id = feed.getId();
        String nickName = feed.getUser().getNickName();
        String profileImageUrl = feed.getUser().getImageUrl();
        Topic category = feed.getCategory();
        String content = feed.getContent();
        List<String> images = feed.getImages() == null
                ? List.of()
                : feed.getImages().stream().map(FeedImage::getImageUrl).toList();
        Integer likeCount = feed.getLikeCount();
        Integer commentCount = feed.getCommentCount();
        boolean mine = feed.getUser().getId().equals(userId);
        var createdAt = feed.getCreatedAt();
        String dateTime = createdAt == null
                ? ""
                : createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return new FeedDetailResponse(id, nickName, profileImageUrl, category, content, images, likedByMe,
                likeCount, commentCount, mine, dateTime);
    }
}
