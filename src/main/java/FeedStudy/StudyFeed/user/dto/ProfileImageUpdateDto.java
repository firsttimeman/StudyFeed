package FeedStudy.StudyFeed.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileImageUpdateDto {

    private MultipartFile profileImage;
    private boolean resetToDefault;
}
