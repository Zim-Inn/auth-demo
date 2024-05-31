package zim.personal.authdemo.exception;

import lombok.Getter;
import zim.personal.authdemo.constant.ResponseCode;

@Getter
public class CustomRuntimeException extends RuntimeException {
    private final ResponseCode errorCode;

    public CustomRuntimeException(String message, ResponseCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
