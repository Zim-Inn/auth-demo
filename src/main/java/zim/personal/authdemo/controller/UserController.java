package zim.personal.authdemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.*;
import zim.personal.authdemo.aspect.annotation.AuthInfo;
import zim.personal.authdemo.constant.ParamResourceGetter;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.domain.User;
import zim.personal.authdemo.dto.Result;
import zim.personal.authdemo.util.CheckBlankUtil;
import zim.personal.authdemo.util.UserDataDao;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@RestController
@RequestMapping("")
@Slf4j
public class UserController implements ApplicationContextAware, ApplicationRunner, Ordered {
    private ApplicationContext context;

    @Autowired
    private UserDataDao userDataDao;

    /**
     * @param user if the userId is null, it will try to insert. Otherwise, it will try to update
     */
    @AuthInfo
    @PostMapping("/admin/insertOrUpdateUser")
    public @ResponseBody Result<User> insertNewUser(@RequestBody User user) {
        if (user == null || CheckBlankUtil.anyMatchBlank(user.getAccountName(), user.getRole())) {
            return Result.fail(ResponseCode.INVALID_REQUEST, "Bad param! The accountName or role can't be empty");
        }
        if (user.getUserId() == null) {
            TreeMap<String, User> data = userDataDao.getALlUserData();
            long last = Long.parseLong(data.lastKey());
            user.setUserId(++last);
            userDataDao.addOneUser(user);
        } else {
            userDataDao.updateOneUser(user);
        }
        return Result.success(userDataDao.getUserById(user.getUserId()));
    }

    @AuthInfo
    @PostMapping("/admin/addUser")
    public @ResponseBody Result<User> addUser(@RequestBody User user) {
        if (user == null || CheckBlankUtil.anyMatchBlank(user.getUserId(), user.getAccountName(), user.getRole())) {
            return Result.fail(ResponseCode.INVALID_REQUEST, "Bad param! The accountName or role can't be empty");
        }
        if (user.getEndpoint() == null) {
            user.setEndpoint(Collections.emptyList());
        }
        userDataDao.updateOneUser(user);
        return Result.success(userDataDao.getUserById(user.getUserId()));
    }

    @AuthInfo(funcToResourcesList = ParamResourceGetter.FIRST_SINGLE_STRING_PARAM)
    @GetMapping("/user/{resource}")
    public @ResponseBody Result<String> access(@PathVariable String resource) {
        return Result.success("You have access to this resource: " + resource);
    }


    /**
     * dirty data will be checked here
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            Map<String, User> data = userDataDao.getALlUserData();
            log.info("Checking dirty data now ......");
            StringBuilder hintMsg = new StringBuilder();
            for (String key : data.keySet()) {
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
