package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ReportException extends BaseException{


    public ReportException(ErrorCode errorCode) {
        super(errorCode);
    }
}
