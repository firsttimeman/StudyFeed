package FeedStudy.StudyFeed.user.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SignUpRequestDto {

    @NotNull(message = "이메일을 입력하셔야 합니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    private String providerType;

    private String providerId;

    private String telecom;

    private String gender;


    @Past(message = "과거 날짜여야 합니다.")
    private LocalDate birthDate;

    private String receiveEvent;

    private String authcode;







}
