package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.feed.entity.Feed;
import FeedStudy.StudyFeed.feed.entity.FeedImage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class FeedDetailResponse {
    private Long id;
    private String category;
    private String content;
    private List<ImageResponseDto> images;
    private List<FeedCommentDto> comments;

    public static FeedDetailResponse toDto(Feed feed) {
        Long id = feed.getId();
        String category = feed.getCategory();
        String content = feed.getContent();
        List<ImageResponseDto> images = feed.getImages().stream().map(i -> new ImageResponseDto(i)).toList();
        List<FeedCommentDto> comments = feed.getComments().stream()
                .map(FeedCommentDto::fromEntity)
                .toList();
        return new FeedDetailResponse(id, category, content, images, comments);
    }
}
