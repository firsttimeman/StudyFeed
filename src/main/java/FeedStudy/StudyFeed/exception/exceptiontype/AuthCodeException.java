package FeedStudy.StudyFeed.exception.exceptiontype;

import FeedStudy.StudyFeed.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AuthCodeException  extends RuntimeException{

    private final ErrorCode errorCode;

    public AuthCodeException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
