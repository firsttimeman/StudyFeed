package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;

public class TokenException extends RuntimeException {
    private final ErrorCode errorCode;

    public TokenException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;

    }
}
