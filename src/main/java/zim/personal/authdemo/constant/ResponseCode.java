package zim.personal.authdemo.constant;

import lombok.Data;

@Data
public class ResponseCode {
    private PrefixEnum codePrefix;
    private String code;


    public int getHttpCode() {
        String str = code.replaceAll("^0+", "");
        if (str.length() >= 3) {
            return Integer.parseInt(str.substring(0, 3));
        } else {
            return 500;
        }
    }

    /**
     * @param codePrefix you should define a new PrefixEnum if existing enums can't describe your error
     * @param code       a positive number that not bigger than 999999
     */
    public ResponseCode(PrefixEnum codePrefix, int code) {
        this.codePrefix = codePrefix;
        code = code < 0 ? -code : code;
        String str = String.valueOf(code);
        if (str.length() > 6) {
            str = str.substring(0, 6);
        } else {
            int i = 6 - str.length();
            str = "0".repeat(i) + code;
        }
        this.code = str;
    }

    /**
     * you should define usual responseCode here
     */
    public static final ResponseCode OK = new ResponseCode(PrefixEnum.BIZ, 0);

    public static final ResponseCode INVALID_REQUEST = new ResponseCode(PrefixEnum.AUTH, 400);

    public static final ResponseCode NO_SUCH_USER = new ResponseCode(PrefixEnum.BIZ, 40001);

    public static final ResponseCode REQUIRE_ADMIN = new ResponseCode(PrefixEnum.AUTH, 40302);

    public static final ResponseCode NO_ACCESS_FOR_RESOURCE = new ResponseCode(PrefixEnum.AUTH, 40303);

    public static final ResponseCode INVALID_USER_TOKEN = new ResponseCode(PrefixEnum.AUTH, 40304);

    public static final ResponseCode FILE_ERROR = new ResponseCode(PrefixEnum.SYS, 5001);

    public static final ResponseCode DATA_FORMAT = new ResponseCode(PrefixEnum.SYS, 5002);


    public static final ResponseCode INTERNAL_SERVER_ERROR = new ResponseCode(PrefixEnum.SYS, 500);

    public enum PrefixEnum {
        BIZ, AUTH,
        NET, SYS
    }

}
