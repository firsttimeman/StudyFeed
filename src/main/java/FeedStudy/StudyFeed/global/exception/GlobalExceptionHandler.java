package FeedStudy.StudyFeed.global.exception;

import FeedStudy.StudyFeed.global.exception.exceptiontype.AuthCodeException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(AuthCodeException.class)
    public ResponseEntity<ErrorResponse> handleAuthCodeException(AuthCodeException e) {
        return buildErrorResponse(e.getErrorCode(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(MemberException e) {
        return buildErrorResponse(e.getErrorCode(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", e.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> collect = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("잘못된 validation", collect.toString(), LocalDateTime.now()));
    }



    private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getDescription(), LocalDateTime.now()));
    }


    @Getter
    @Setter
    public static class ErrorResponse {
        private final String errorCode;
        private final String errorMessage;
        private final LocalDateTime timestamp;

        public ErrorResponse(String errorCode, String errorMessage, LocalDateTime timestamp) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.timestamp = timestamp;
        }

    }

}
