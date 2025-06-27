package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import lombok.Getter;

@Getter

public class KeyException extends BaseException {

    public KeyException(ErrorCode errorCode) {
        super(errorCode);
    }
}
