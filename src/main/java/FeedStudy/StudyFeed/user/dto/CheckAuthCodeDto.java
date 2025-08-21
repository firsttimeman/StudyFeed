package FeedStudy.StudyFeed.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "인증번호 검증 dto")
public class CheckAuthCodeDto {

    @NotNull(message = "이메일을 입력하셔야 합니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Schema(description = "유저 이메일")
    private String email;

    @Schema(description = "이메일 인증 암호")
    private String code;

}
