package zim.personal.authdemo.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.dto.Result;
import zim.personal.authdemo.exception.CustomRuntimeException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        // print that unhandled exception's info
        log.error(e.getMessage(), e);
        if (e instanceof JsonProcessingException) {
            return new ResponseEntity<>(Result.fail(ResponseCode.INTERNAL_SERVER_ERROR, "an error occurred while processing json"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(Result.fail(ResponseCode.INTERNAL_SERVER_ERROR, "internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<Result<?>> handleCustomRuntimeException(CustomRuntimeException exception) {
        ResponseCode errorCode = exception.getErrorCode();
        return new ResponseEntity<>(Result.fail(errorCode, exception.getMessage()), HttpStatus.valueOf(errorCode.getHttpCode()));
    }

}
