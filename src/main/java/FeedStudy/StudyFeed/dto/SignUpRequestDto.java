package FeedStudy.StudyFeed.dto;

import FeedStudy.StudyFeed.exception.ValidEnum;
import FeedStudy.StudyFeed.type.Gender;
import FeedStudy.StudyFeed.type.Telecom;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {

    @NotNull(message = "이메일을 입력하셔야 합니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @NotNull(message = "닉네임을 설정해주세요.")
    private String nickName;

    @NotNull(message = "올바른 코드를 입력해주세요")
    private String authcode;

    @NotNull(message = "비밀번호를 입력하셔야 합니다,")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;

    @NotNull(message = "통신사를 입력하세요")
    @ValidEnum(enumClass = Telecom.class, message = "올바른 통신사를 입력하세요")
    private Telecom telecom;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-XXXX-YYYY이어야 합니다.")
    private String phoneNumber;

    @NotNull(message = "성별을 입력하세요")
    @ValidEnum(enumClass = Gender.class, message = "올바른 성별을 입력하세요")
    private Gender gender;
}
