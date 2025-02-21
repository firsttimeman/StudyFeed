package FeedStudy.StudyFeed.dto;

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
public class FeedEditRequest {


    private String content;

    @NotBlank
    private String category;

    private List<MultipartFile> addImages = new ArrayList<>();

    private List<Integer> deletedImages = new ArrayList<>();
}
