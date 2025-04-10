package FeedStudy.StudyFeed.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {


    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일을 반드시 입력해야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호를 반드시 입력해야 합니다.")
    private String password;
}
