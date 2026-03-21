package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;

public class S3Exception extends BaseException{

    public S3Exception(ErrorCode errorCode) {
        super(errorCode);
    }
}
