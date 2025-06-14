package FeedStudy.StudyFeed.feed.dto;

import jakarta.validation.constraints.NotBlank;
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
    private String content;

    @NotBlank
    private String category;

    private List<MultipartFile> addedImages = new ArrayList<>();

    private List<String> deletedImages = new ArrayList<>();
}
