package zim.personal.authdemo.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Util {

    /**
     * 使用URL安全的Base64编码方式将字符串编码为Base64。
     *
     * @param data 要编码的字符串
     * @return Base64编码后的字符串
     */
    public static String encode(String data) {
        return Base64.getUrlEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用URL安全的Base64解码方式将Base64编码的字符串解码为原始字符串。
     *
     * @param encodedData Base64编码的字符串
     * @return 解码后的原始字符串
     */
    public static String decode(String encodedData) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedData);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    // 测试Base64编码和解码
    public static void main(String[] args) {
        String originalData = "Hello, World!";
        String encodedData = encode(originalData);
        String decodedData = decode(encodedData);

        System.out.println("Original Data: " + originalData);
        System.out.println("Encoded Data: " + encodedData);
        System.out.println("Decoded Data: " + decodedData);
    }
}
