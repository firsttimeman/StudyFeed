package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;

public class PolicyException extends BaseException {
    public PolicyException(ErrorCode errorCode) {
        super(errorCode);
    }
}
