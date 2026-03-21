package FeedStudy.StudyFeed.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequestDto {

    @NotBlank(message = "이메일을 입력하셔야 합니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Schema(description = "유저 이메일")
    private String email;


    @NotBlank(message = "비밀번호를 입력하셔야 합니다.")
    @Schema(description = "비밀번호")
    private String password;
}
