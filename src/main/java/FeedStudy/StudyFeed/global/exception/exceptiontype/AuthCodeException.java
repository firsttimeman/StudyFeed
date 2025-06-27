package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AuthCodeException  extends BaseException{


    public AuthCodeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
