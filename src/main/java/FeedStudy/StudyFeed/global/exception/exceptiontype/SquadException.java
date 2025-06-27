package FeedStudy.StudyFeed.global.exception.exceptiontype;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SquadException extends BaseException{


    public SquadException(ErrorCode errorCode) {
        super(errorCode);
    }
}
