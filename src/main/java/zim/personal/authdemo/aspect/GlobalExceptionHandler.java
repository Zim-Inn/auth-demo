package zim.personal.authdemo.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.dto.Result;
import zim.personal.authdemo.exception.CustomRuntimeException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // print that unhandled exception's info
        log.error(e.getMessage(), e);
        if (e instanceof JsonProcessingException) {
            return Result.fail(ResponseCode.INTERNAL_SERVER_ERROR, "an error occurred while processing json");
        }
        return Result.fail(ResponseCode.INTERNAL_SERVER_ERROR, "internal server error");
    }

    @ExceptionHandler(CustomRuntimeException.class)
    public Result<?> handleCustomRuntimeException(CustomRuntimeException exception) {
        return Result.fail(exception.getErrorCode(), exception.getMessage());
    }

}
