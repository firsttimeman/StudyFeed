package FeedStudy.StudyFeed.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "회원가입 dto")
public class SignUpRequestDto {

    @NotNull(message = "이메일을 입력하셔야 합니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Schema(description = "유저 이메일")
    private String email;

    @Schema(description = "소셜 종류")
    private String providerType;

    @Schema(description = "소셜 고유 아이디")
    private String providerId;

    @Schema(description = "통신사")
    private String telecom;

    @Schema(description = "성별")
    private String gender;


    @Past(message = "과거 날짜여야 합니다.")
    @Schema(description = "생년월일")
    private LocalDate birthDate;

    @Schema(description = "이벤트 수신 여부")
    private String receiveEvent;

    @Schema(description = "이메일 인증 암호")
    private String authcode;







}
