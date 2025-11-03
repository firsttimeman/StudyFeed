package FeedStudy.StudyFeed.feed.dto;

import FeedStudy.StudyFeed.global.type.Topic;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedRequest {


    @NotBlank
    @Size(max = 1000)
    @Schema(description = "피드 내용을 입력한다.")
    private String content;

    @NotNull
    @Schema(description = "피드에 등록할 카테고리")
    private Topic category;


    @Schema(description = "피드에 추가할 이미지")
    private List<MultipartFile> addedImages = new ArrayList<>();

    @Schema(description = "추후 피드 변경시 이미지")
    private List<String> deletedImages = new ArrayList<>();
}
