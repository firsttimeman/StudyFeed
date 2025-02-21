package FeedStudy.StudyFeed.exception.exceptiontype;

import FeedStudy.StudyFeed.exception.ErrorCode;

public class FeedException extends RuntimeException {

    private final ErrorCode errorCode;

    public FeedException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
