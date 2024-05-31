package zim.personal.authdemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zim.personal.authdemo.aspect.annotation.AuthInfo;
import zim.personal.authdemo.aspect.annotation.AuthType;
import zim.personal.authdemo.constant.ParamResourceGetter;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.domain.User;
import zim.personal.authdemo.dto.Result;
import zim.personal.authdemo.util.CheckBlankUtil;
import zim.personal.authdemo.util.UserDataDao;

import java.util.Collections;

@RestController
@RequestMapping("")
@Slf4j
public class UserController {

    @Autowired
    private UserDataDao userDataDao;

    /**
     * @param user if the userId is null, it will try to insert. Otherwise, it will try to update
     */
    @AuthInfo(type = AuthType.ONLY_ADMIN)
    @PostMapping("/admin/insertOrUpdateUser")
    public Result<User> insertNewUser(@RequestBody User user) {
        if (user == null || CheckBlankUtil.anyMatchBlank(user.getAccountName(), user.getRole())) {
            return Result.fail(ResponseCode.INVALID_REQUEST, "Bad param! The accountName or role can't be empty");
        }
        if (user.getUserId() == null) {
            user.setUserId(userDataDao.getNextUserIdForInsert());
            userDataDao.addOneUser(user);
        } else {
            userDataDao.updateOneUser(user);
        }
        return Result.success(userDataDao.getUserById(user.getUserId()));
    }

    @AuthInfo(type = AuthType.ONLY_ADMIN)
    @PostMapping("/admin/addUser")
    public Result<User> addUser(@RequestBody User user) {
        if (user == null || CheckBlankUtil.anyMatchBlank(user.getUserId(), user.getAccountName(), user.getRole())) {
            return Result.fail(ResponseCode.INVALID_REQUEST, "Bad param! The accountName or role can't be empty");
        }
        if (user.getEndpoint() == null) {
            user.setEndpoint(Collections.emptyList());
        }
        if (userDataDao.updateOneUser(user)) {
            return Result.success(userDataDao.getUserById(user.getUserId()));
        } else {
            return Result.fail(ResponseCode.NO_SUCH_USER, "No such user with userId = " + user.getUserId());
        }

    }

    @AuthInfo(funcToResourcesList = ParamResourceGetter.FIRST_SINGLE_STRING_PARAM)
    @GetMapping("/user/{resource}")
    public Result<String> access(@PathVariable String resource) {
        return Result.success("You have access to this resource: " + resource);
    }

}
