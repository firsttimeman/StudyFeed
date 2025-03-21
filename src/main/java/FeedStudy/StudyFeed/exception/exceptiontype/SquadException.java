package FeedStudy.StudyFeed.exception.exceptiontype;

import FeedStudy.StudyFeed.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SquadException extends RuntimeException{

    private final ErrorCode errorCode;

    public SquadException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
