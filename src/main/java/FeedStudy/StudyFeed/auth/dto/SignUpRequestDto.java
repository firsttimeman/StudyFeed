package FeedStudy.StudyFeed.auth.dto;

import FeedStudy.StudyFeed.global.type.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "회원가입 dto")
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {

    @NotNull(message = "이메일을 입력하셔야 합니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Schema(description = "유저 이메일")
    private String email;


    @NotBlank(message = "비밀번호를 입력하셔야 합니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하이어야 합니다.")
    private String password;

    @NotNull(message = "성별을 선택해주세요.")
    private Gender gender;

    @Past(message = "과거 날짜여야 합니다.")
    @Schema(description = "생년월일")
    private LocalDate birth;

    @Schema(description = "이벤트 수신 여부")
    private String receiveEvent;



}
