package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class NoticeException extends BaseException {

    public NoticeException(ErrorCode errorCode) {
        super(errorCode);
    }
}