package zim.personal.authdemo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import zim.personal.authdemo.config.DataProperties;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.domain.User;
import zim.personal.authdemo.exception.CustomRuntimeException;

import java.nio.file.FileSystems;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiPredicate;

/*
 * read or update json format data via the data file
 */
@Slf4j
@Getter
@Component
public class UserDataDao implements ApplicationContextAware, ApplicationRunner, Ordered {

    private ApplicationContext context;

    private static final String DATA_DIR = "data";

    private static final String USER_DATA = "userData.json";

    private final ResourceFileOperator operator;

    private ConcurrentSkipListMap<String, User> userDataCache;
    private static final String DEFAULT_USER_STR = "{\"-3154\":{\"userId\":-3154,\"role\":\"admin\",\"accountName\":\"zim\",\"endpoint\":[]}}";


    public UserDataDao(DataProperties properties) {
        String path;
        if (!CheckBlankUtil.isBlank(properties.getUser())) {
            path = properties.getUser();
            this.operator = new ResourceFileOperator(path);
        } else {
            this.operator = new ResourceFileOperator(DATA_DIR + FileSystems.getDefault().getSeparator() + USER_DATA);
        }
        if (!this.operator.getResource().exists() || getALlUserData().isEmpty()) {
            // write a default admin user.
            // set its userId as -3154 so that ensure external attackers can't hit the default id
            // and other users' id will start from 1 and auto increase.
            this.operator.writeResourceFile(DEFAULT_USER_STR);
        }
        userDataCache = getALlUserData();
    }

    private ConcurrentSkipListMap<String, User> getALlUserData() {
        try {
            String content = operator.readFileFromResources();
            return JsonUtil.deserialize(content, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("read a invalid data file, relevant path is {}", DATA_DIR + FileSystems.getDefault().getSeparator() + USER_DATA, e);
            throw new CustomRuntimeException("read invalid data file", ResponseCode.DATA_FORMAT);
        }
    }

    /**
     * @return the last userId + 1
     */
    public Long getNextUserIdForInsert() {
        long id = Long.parseLong(userDataCache.lastKey());
        id = id < 0 ? 0 : id;
        return ++id;
    }


    public User getUserById(long userId) {
        return userDataCache.get(userId + "");
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
        return operateOne(user, (key, map) -> map.containsKey(key));
    }

    /**
     * @param users if any user was existing, that row wouldn't be inserted
     * @return count of inserted rows
     */
    public int addUsersBatch(List<User> users) {
        if (users.stream().anyMatch(UserDataDao::testInvalidUserParam)) {
            throw new CustomRuntimeException("invalid args! userId, role or accountName should be nonempty", ResponseCode.INTERNAL_SERVER_ERROR);
        }
        List<String> rollBackKeys = new ArrayList<>();
        for (User user : users) {
            String key = user.getUserId() + "";
            if (!userDataCache.containsKey(key)) {
                userDataCache.put(key, user);
                rollBackKeys.add(key);
            }
        }
        try {
            operator.writeResourceFile(JsonUtil.serialize(userDataCache), () -> rollBackKeys.forEach(userDataCache::remove));
        } catch (JsonProcessingException e) {
            throw new CustomRuntimeException("json serialize error", ResponseCode.INTERNAL_SERVER_ERROR);
        }

        return rollBackKeys.size();
    }

    private boolean operateOne(User user, BiPredicate<String, Map<String, User>> opAbandonTester) {
        if (testInvalidUserParam(user)) {
            throw new CustomRuntimeException("invalid args! userId, role or accountName should be nonempty", ResponseCode.INTERNAL_SERVER_ERROR);
        }
        String key = user.getUserId() + "";
        if (opAbandonTester.test(key, userDataCache)) {
            return false;
        } else {
            try {
                userDataCache.put(key, user);
                operator.writeResourceFile(JsonUtil.serialize(userDataCache), () -> userDataCache.remove(key));
            } catch (JsonProcessingException e) {
                throw new CustomRuntimeException("json serialize error", ResponseCode.INTERNAL_SERVER_ERROR);
            }
        }

        return true;
    }

    /**
     * @param user existing user
     * @return true if a row has been updated
     */
    public boolean updateOneUser(User user) {
        return operateOne(user, (key, map) -> !map.containsKey(key));
    }

    @PreDestroy
    public void cleanDataWhenTest() throws JsonProcessingException {
        String profile = ConfigUtil.getProperty("spring.profiles.active");
        if ("test".equals(profile)) {
            operator.writeResourceFile(DEFAULT_USER_STR);
            userDataCache = getALlUserData();
        } else {
            operator.writeResourceFile(JsonUtil.serialize(userDataCache));
        }
    }

    /**
     * dirty data will be checked here
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            Map<String, User> data = userDataCache;
            log.info("Checking dirty data now ......");
            StringBuilder hintMsg = new StringBuilder();
            for (String key : data.keySet()) {
                // I don't check the value of role here. Non-defined value will be regard as common user.
                try {
                    Long id = Long.parseLong(key);
                    User user = data.get(key);
                    if (user == null) {
                        hintMsg.append("key:").append(key).append("found null data.").append(System.lineSeparator());
                    } else if (!Objects.equals(user.getUserId(), id)) {
                        hintMsg.append("key:").append(key).append("found different key and object id data").append(System.lineSeparator());
                    }
                } catch (NumberFormatException e) {
                    hintMsg.append("key:").append(key).append("found non number key.").append(System.lineSeparator());
                }
            }
            if (hintMsg.isEmpty()) {
                log.info("Finished data check. Everything is ok");
            } else {
                log.error("Found dirty data:{}{}", System.lineSeparator(), hintMsg);
                log.info("The application won't be run if the dirty data is existing");
                ((ConfigurableApplicationContext) this.context).close();
                System.exit(1);
            }
        } catch (Exception e) {
            log.error("Data check failed!", e);
            log.info("The application won't be run if it can't be ensured that there is no dirty data");
            ((ConfigurableApplicationContext) this.context).close();
            System.exit(1);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
