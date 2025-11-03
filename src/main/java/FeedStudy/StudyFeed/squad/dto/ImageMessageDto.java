package FeedStudy.StudyFeed.squad.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class ImageMessageDto {

    @NotEmpty(message = "imageUrls 는 비어있을 수 없습니다.")
    private List<@Pattern(regexp = "https?://.+", message = "imageUrls 항목은 유효한 http(s) URL 이어야 합니다.")String>
            imageUrls;
}