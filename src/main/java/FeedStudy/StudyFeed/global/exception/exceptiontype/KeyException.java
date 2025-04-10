package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import lombok.Getter;

@Getter

public class KeyException extends RuntimeException {
    private final ErrorCode errorCode;

    public KeyException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
