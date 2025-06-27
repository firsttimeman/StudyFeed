package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;

public class BlockException extends BaseException{

    public BlockException(ErrorCode errorCode) {
        super(errorCode);
    }
}
