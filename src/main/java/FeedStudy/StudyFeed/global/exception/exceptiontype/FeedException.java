package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;

public class FeedException extends BaseException {


    public FeedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
