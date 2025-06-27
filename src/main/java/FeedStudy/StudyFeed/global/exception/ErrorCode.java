package FeedStudy.StudyFeed.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 회원 관련
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용중인 이메일입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "맞는 사용자가 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "PASSWORD_NOT_MATCH", "비밀번호가 틀립니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "NICKNAME_ALREADY_EXISTS", "닉네임이 존재합니다"),
    NICKNAME_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NICKNAME_GENERATION_FAILED", "닉네임 생성에 실패했습니다."),

    // 인증 / 토큰 / 키
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "액세스 토큰이 존재하지 않습니다."),
    AUTH_CODE_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_CODE_MISMATCH", "인증 번호가 맞지 않습니다."),
    KEY_NOT_EXIST(HttpStatus.NOT_FOUND, "KEY_NOT_EXIST", "키가 존재하지 않습니다"),
    KEY_LOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "KEY_LOAD_ERROR", "키 로드중 오류가 생겼습니다."),

    // 이미지 / 파일
    IMAGE_EXT_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "IMAGE_EXT_NOT_SUPPORTED", "지원되지 않는 이미지 형식입니다."),
    FILE_CANNOT_BE_UPLOADED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_CANNOT_BE_UPLOADED", "파일 업로드가 되지 않습니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "INVALID_FILE_NAME", "잘못된 파일 이름입니다."),
    IMAGE_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_URL_GENERATION_FAILED", "이미지 URL 생성에 실패했습니다."),

    // 피드 / 신고
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "FEED_NOT_FOUND", "피드를 찾을 수 없습니다."),
    NOT_FEED_USER(HttpStatus.FORBIDDEN, "NOT_FEED_USER", "피드 작성자가 아닙니다."),
    ALREADY_REPORTED(HttpStatus.BAD_REQUEST, "ALREADY_REPORTED", "이미 신고가 된 피드입니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "신고 피드가 존재하지 않습니다."),
    BANNED_USER(HttpStatus.FORBIDDEN, "BANNED_USER", "차단된 유저입니다."),

    // 모임 조건
    UNABLE_TO_USE_MIN_MAX_AGE(HttpStatus.BAD_REQUEST, "UNABLE_TO_USE_MIN_MAX_AGE", "나이대를 선택했으면 min/maxAge를 입력할 수 없습니다."),
    MIN_MAX_AGE_ERROR(HttpStatus.BAD_REQUEST, "MIN_MAX_AGE_ERROR", "최소 나이는 최대 나이보다 작아야 합니다."),
    GENDER_ERROR(HttpStatus.BAD_REQUEST, "GENDER_ERROR", "해당 모임은 특정 성별만 가입할 수 있습니다."),

    // FCM
    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "FCM_TOKEN_NOT_FOUND", "등록되지 않은 fcmToken 입니다."),

    // 메일
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL_SEND_FAILED", "메일 전송에 실패했습니다."),

    // 차단
    BLOCK_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "BLOCK_ALREADY_EXISTS", "이미 차단된 사용자입니다."),
    BLOCK_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "BLOCK_SELF_NOT_ALLOWED", "자기 자신은 차단할 수 없습니다."),
    BLOCK_NOT_FOUND(HttpStatus.BAD_REQUEST, "BLOCK_NOT_FOUND", "차단 정보가 존재하지 않습니다."),

    // 댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글이 존재하지 않습니다."),
    NOT_COMMENT_OWNER(HttpStatus.FORBIDDEN, "NOT_COMMENT_OWNER", "댓글 삭제 권한이 없습니다."),

    // 스쿼드 (모임)
    SQUAD_NOT_FOUND(HttpStatus.NOT_FOUND, "SQUAD_NOT_FOUND", "해당 모임을 찾을 수 없습니다."),
    ALREADY_JOINED(HttpStatus.BAD_REQUEST, "ALREADY_JOINED", "이미 참여중인 사용자입니다."),
    SQUAD_CLOSED(HttpStatus.BAD_REQUEST, "SQUAD_CLOSED", "마감된 모임입니다."),
    SQUAD_TIME_PASSED(HttpStatus.BAD_REQUEST, "SQUAD_TIME_PASSED", "종료된 모임입니다."),
    SQUAD_FULL(HttpStatus.BAD_REQUEST, "SQUAD_FULL", "참여 인원이 찼습니다."),
    SQUAD_AGE_CONFLICT(HttpStatus.BAD_REQUEST, "SQUAD_AGE_CONFLICT", "이미 참여 중인 다른 나이대의 멤버가 있어서 수정이 어려워요."),
    AGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AGE_NOT_ALLOWED", "참여 가능한 연령이 아닙니다."),
    SQUAD_GENDER_CONFLICT(HttpStatus.BAD_REQUEST, "SQUAD_GENDER_CONFLICT", "이미 참여 중인 다른 성별의 멤버가 있어서 수정이 어렵습니다."),
    GENDER_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "GENDER_NOT_ALLOWED", "참여 가능한 성별이 아닙니다."),
    SQUAD_MEMBER_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "SQUAD_MEMBER_COUNT_EXCEEDED", "현재 참여 인원이 수정하려는 최대 인원을 초과합니다."),
    SQUAD_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SQUAD_DELETE_NOT_ALLOWED", "모든 멤버를 내보낸 후에만 삭제할 수 있습니다."),
    NOT_APPROVAL_SQUAD(HttpStatus.BAD_REQUEST, "NOT_APPROVAL_SQUAD", "승인제가 아닌 모임입니다."),
    NOT_SQUAD_OWNER(HttpStatus.FORBIDDEN, "NOT_SQUAD_OWNER", "모임의 소유자가 아닙니다."),
    AGE_RANGE_INVALID(HttpStatus.BAD_REQUEST, "AGE_RANGE_INVALID", "최소 나이는 최대 나이보다 작아야 합니다."),
    SQUAD_REJECTED(HttpStatus.BAD_REQUEST, "SQUAD_REJECTED", "해당 모임 참여가 거절된 사용자입니다."),
    SQUAD_KICKED_OUT(HttpStatus.FORBIDDEN, "SQUAD_KICKED_OUT", "강퇴된 사용자입니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_MESSAGE_NOT_FOUND", "메시지를 찾을 수 없습니다."),
    NOT_CHAT_OWNER(HttpStatus.FORBIDDEN, "NOT_CHAT_OWNER", "메시지 삭제 권한이 없습니다."),
    IMAGE_UPLOAD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "IMAGE_UPLOAD_LIMIT_EXCEEDED", "최대 10장까지 업로드할 수 있습니다."),
    SQUAD_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "SQUAD_MEMBER_NOT_FOUND", "모임 멤버를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String description;
}