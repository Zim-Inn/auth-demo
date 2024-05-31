package zim.personal.authdemo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import zim.personal.authdemo.domain.User;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象转换为JSON字符串。
     *
     * @param obj 要序列化的对象
     * @return JSON字符串
     * @throws JsonProcessingException 如果序列化过程中发生错误
     */
    public static String serialize(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 将JSON字符串转换为指定类型的对象。
     *
     * @param json  JSON字符串
     * @param clazz 目标对象的类类型
     * @param <T>   目标对象的类型
     * @return 反序列化后的对象
     * @throws JsonProcessingException 如果反序列化过程中发生错误
     */
    public static <T> T deserialize(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * 将JSON字符串转换为指定类型的对象。
     *
     * @param json         JSON字符串
     * @param valueTypeRef generic type
     * @param <T>          目标对象的类型
     * @return 反序列化后的对象
     * @throws JsonProcessingException 如果反序列化过程中发生错误
     */
    public static <T> T deserialize(String json, TypeReference<T> valueTypeRef) throws JsonProcessingException {
        return objectMapper.readValue(json, valueTypeRef);
    }

    // 测试JSON序列化和反序列化
    public static void main(String[] args) {
        User person = new User();
        try {
            String jsonString = serialize(person);
            System.out.println("Serialized JSON: " + jsonString);

            User deserializedUser = deserialize(jsonString, User.class);
            System.out.println("Deserialized Person: " + deserializedUser);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}

