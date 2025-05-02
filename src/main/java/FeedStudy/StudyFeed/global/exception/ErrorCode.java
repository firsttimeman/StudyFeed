package FeedStudy.StudyFeed.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "이미 사용중인 이메일입니다"),
    USER_NOT_FOUND("USER_NOT_FOUND", "맞는 사용자가 없습니다."),
    PASSWORD_NOT_MATCH( "PASSWORD_NOT_MATCH", "비밀번호가 틀립니다."),
    NICKNAME_ALREADY_EXISTS("NICKNAME_ALREADY_EXISTS", "닉네임이 존재합니다"),
    NICKNAME_GENERATION_FAILED("NICKNAME_GENERATION_FAILED", "닉네임 생성에 실패했습니다."),
    INVALID_ACCESS_TOKEN("INVALID_ACCESS_TOKEN", "액세스 토큰이 존재하지 않습니다."),

    AUTH_CODE_MISMATCH("AUTH_CODE_MISMATCH", "인증 번호가 맞지 않습니다."),
    KEY_NOT_EXIST("KEY_NOT_EXIST", "키가 존재하지 않습니다"),
    KEY_LOAD_ERROR( "KEY_LOAD_ERROR", "키 로드중 오류가 생겼습니다."),

    IMAGE_EXT_NOT_SUPPORTED("IMAGE_EXT_NOT_SUPPORTED", "지원되지 않는 이미지 형식입니다."),
    FILE_CANNOT_BE_UPLOADED("FILE_CANNOT_BE_UPLOADED", "파일 업로드가 되지 않습니다."),
    FEED_NOT_FOUND("FEED_NOT_FOUND", "피드를 찾을수가 없습니다."),
    NOT_FEED_USER("NOT_FEED_USER", "피드 작성자가 아닙니다."),
    ALREADY_REPORTED("ALREADY_REPORTED", "이미 신고가 된 피드입니다."),
    REPORT_NOT_FOUND("REPORT_NOT_FOUND", "신고 피드가 존재하지 않습니다."),
    BANNED_USER("BANNED_USER", "차단된 유저입니다."),

    UNABLE_TO_USE_MIN_MAX_AGE("UNABLE_TO_USE_MIN_MAX_AGE", "나이대를 선택했으면 min/maxAge를 입력할수가 없습니다."),
    MIN_MAX_AGE_ERROR("MIN_MAX_AGE_ERROR", "최소 나이는 최대 나이보다 작아야 합니다."),
    GENDER_ERROR("GENDER_ERROR", "해당 모임은 특정 성별만 가입할 수 있습니다."),

    FCM_TOKEN_NOT_FOUND( "FCM_TOKEN_NOT_FOUND","등록되지 않은 fcmToken 입니다.");


    private final String code;
    private final String description;
}
