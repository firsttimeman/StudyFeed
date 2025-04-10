package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MemberException extends RuntimeException {
    private final ErrorCode errorCode;

    public MemberException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
