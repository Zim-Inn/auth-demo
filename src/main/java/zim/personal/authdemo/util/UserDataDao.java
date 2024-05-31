package zim.personal.authdemo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zim.personal.authdemo.config.DataProperties;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.domain.User;
import zim.personal.authdemo.exception.CustomRuntimeException;

import java.nio.file.FileSystems;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;

/*
 * read or update json format data via the data file
 */
@Slf4j
@Component
public class UserDataDao {

    private static final String DATA_DIR = "data";

    private static final String USER_DATA = "userData.json";

    private final ResourceFileOperator operator;


    public UserDataDao(DataProperties properties) {
        String path;
        if (!CheckBlankUtil.isBlank(properties.getUser())) {
            path = properties.getUser();
            this.operator = new ResourceFileOperator(path);
        } else {
            this.operator = new ResourceFileOperator(DATA_DIR + FileSystems.getDefault().getSeparator() + USER_DATA);
        }
        if (!this.operator.getResource().exists()) {
            this.operator.writeResourceFile("{}");
        }
    }

    public TreeMap<String, User> getALlUserData() {
        try {
            String content = operator.readFileFromResources();
            return JsonUtil.deserialize(content, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("read a invalid data file, relevant path is {}", DATA_DIR + FileSystems.getDefault().getSeparator() + USER_DATA, e);
            throw new CustomRuntimeException("read invalid data file", ResponseCode.DATA_FORMAT);
        }
    }


    public User getUserById(long userId) {
        return getALlUserData().get(userId + "");
    }


    /**
     * @param user
     * @return true if the param is invalid
     */
    private static boolean testInvalidUserParam(User user) {
        return CheckBlankUtil.anyMatchBlank(user.getUserId(), user.getRole(), user.getAccountName());
    }

    /**
     * @param user data
     * @return false if the existed same userId; true if the data was inserted
     */
    public boolean addOneUser(User user) {
        return operateOne(user, (key, map) -> !map.containsKey(key));
    }

    /**
     * @param users if any user was existing, that row wouldn't be inserted
     * @return count of inserted rows
     */
    public int addUsersBatch(List<User> users) {
        if (users.stream().anyMatch(UserDataDao::testInvalidUserParam)) {
            throw new CustomRuntimeException("invalid args! userId, role or accountName should be nonempty", ResponseCode.INTERNAL_SERVER_ERROR);
        }
        Map<String, User> userMap = getALlUserData();
        int count = 0;
        for (User user : users) {
            String key = user.getUserId() + "";
            if (!userMap.containsKey(key)) {
                userMap.put(key, user);
                count++;
            }
        }
        try {
            operator.writeResourceFile(JsonUtil.serialize(userMap));
        } catch (JsonProcessingException e) {
            throw new CustomRuntimeException("json serialize error", ResponseCode.INTERNAL_SERVER_ERROR);
        }

        return count;
    }

    private boolean operateOne(User user, BiPredicate<String, Map<String, User>> opAbandonTester) {
        if (testInvalidUserParam(user)) {
            throw new CustomRuntimeException("invalid args! userId, role or accountName should be nonempty", ResponseCode.INTERNAL_SERVER_ERROR);
        }
        Map<String, User> userMap = getALlUserData();
        String key = user.getUserId() + "";
        if (opAbandonTester.test(key, userMap)) {
            return false;
        } else {
            userMap.put(key, user);
        }
        try {
            operator.writeResourceFile(JsonUtil.serialize(userMap));
        } catch (JsonProcessingException e) {
            throw new CustomRuntimeException("json serialize error", ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return true;
    }

    public boolean updateOneUser(User user) {
        return operateOne(user, (key, map) -> map.containsKey(key));
    }

}
