package FeedStudy.StudyFeed.exception.exceptiontype;

import FeedStudy.StudyFeed.exception.ErrorCode;

public class TokenException extends RuntimeException {
    private final ErrorCode errorCode;

    public TokenException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;

    }
}
