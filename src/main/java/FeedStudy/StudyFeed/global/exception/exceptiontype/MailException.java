package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;

public class MailException extends BaseException{

    public MailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
