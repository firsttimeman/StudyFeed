package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SquadException extends RuntimeException{

    private final ErrorCode errorCode;

    public SquadException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
