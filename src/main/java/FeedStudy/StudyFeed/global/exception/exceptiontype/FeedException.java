package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;

public class FeedException extends RuntimeException {

    private final ErrorCode errorCode;

    public FeedException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
