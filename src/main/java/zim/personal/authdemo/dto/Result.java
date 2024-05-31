package zim.personal.authdemo.dto;


import lombok.Getter;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.util.ValuePicker;

import java.util.HashMap;

@Getter
public class Result<T> {

    private final String responseCode;

    private final String responseMsg;

    private final Object responseData;

    private Result(ResponseCode code, String responseMsg, Object data) {
        this.responseCode = code.getCodePrefix() + "_" + code.getCode();
        this.responseMsg = responseMsg;
        this.responseData = ValuePicker.pickOrLazyDef(data, HashMap::new);
    }


    public static <T> Result<T> success(T responseBody) {
        return new Result<>(ResponseCode.OK, "success", responseBody);
    }

    public static <T> Result<T> success(String responseMsg, T responseBody) {
        return new Result<>(ResponseCode.OK, responseMsg, responseBody);
    }

    public static <T> Result<T> fail(ResponseCode code, String responseMsg) {
        return new Result<>(code, responseMsg, new HashMap<>());
    }


}
