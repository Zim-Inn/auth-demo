package zim.personal.authdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.domain.User;
import zim.personal.authdemo.dto.Result;
import zim.personal.authdemo.util.Base64Util;
import zim.personal.authdemo.util.JsonUtil;
import zim.personal.authdemo.util.UserDataDao;


import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    private static final User BASE_USER;

    static {
        try {
            BASE_USER = JsonUtil.deserialize("""
                    {
                        "role": "user",
                        "accountName": "ne",
                        "endpoint": []
                    }
                    """, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    private static User copyFrom(User source) {
        User target = new User();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    @Autowired
    private UserDataDao userDataDao;

    @Autowired
    private MockMvc mockMvc;

    private static final String DEFAULT_ADMIN_USER_TOKEN = "eyJ1c2VySWQiOi0zMTU0LCJyb2xlIjoiYWRtaW4iLCJhY2NvdW50TmFtZSI6InppbSIsImVuZHBvaW50IjpbXX0=";


    @Test
    public void shouldInsertNewUser() throws Exception {
        insertOne();
    }

    /**
     * first call will insert that userId = 1
     */
    private void insertOne() throws Exception {
        Long id = userDataDao.getNextUserIdForInsert();
        User expect = copyFrom(BASE_USER);
        expect.setUserId(id);
        mockMvc.perform(
                        post("/admin/insertOrUpdateUser")
                                .header("USER-TOKEN", DEFAULT_ADMIN_USER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(BASE_USER)))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.serialize(Result.success(expect))));
    }

    @Test
    public void shouldGrantUserResource() throws Exception {
        // second call will insert that userId = 2
        insertOne();
        grantResource();
    }

    private void grantResource() throws Exception {
        User expect = copyFrom(BASE_USER);
        expect.setUserId(1L);
        expect.setEndpoint(Arrays.asList("resourceA", "resourceB"));
        mockMvc.perform(
                        post("/admin/addUser")
                                .header("USER-TOKEN", DEFAULT_ADMIN_USER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(expect)))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.serialize(Result.success(expect))));
    }

    @Test
    public void shouldHaveAccessForUser() throws Exception {
        insertOne();
        grantResource();
        User actual = copyFrom(BASE_USER);
        actual.setUserId(1L);
        String token = Base64Util.encode(JsonUtil.serialize(actual));
        String resource = "resourceA";
        mockMvc.perform(
                        get("/user/" + resource)
                                .header("USER-TOKEN", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.serialize(Result.success("You have access to this resource: " + resource))));
    }


    @Test
    public void shouldNotHaveAccessForUser() throws Exception {
        insertOne();
        grantResource();
        User actual = copyFrom(BASE_USER);
        actual.setUserId(1L);
        String token = Base64Util.encode(JsonUtil.serialize(actual));
        String resource = "addUser";
        mockMvc.perform(
                        get("/user/" + resource)
                                .header("USER-TOKEN", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(JsonUtil.serialize(Result.fail(ResponseCode.NO_ACCESS_FOR_RESOURCE, "You have no access for : " + resource))));
    }

    @Test
    public void shouldNoticeNoTargetUser() throws Exception {
        insertOne();
        grantResource();
        User expect = copyFrom(BASE_USER);
        expect.setUserId(2L);
        expect.setEndpoint(Arrays.asList("/api/abc", "/api/ee"));
        mockMvc.perform(
                        post("/admin/addUser")
                                .header("USER-TOKEN", DEFAULT_ADMIN_USER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(expect)))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.serialize(Result.fail(ResponseCode.NO_SUCH_USER, "No such user with userId = " + expect.getUserId()))));
    }

    @Test
    public void shouldNoticeInvalidToken() throws Exception {
        User actual = copyFrom(BASE_USER);
        actual.setUserId(2L);
        actual.setRole("admin");
        String token = Base64Util.encode(JsonUtil.serialize(actual));
        mockMvc.perform(
                        post("/admin/addUser")
                                .header("USER-TOKEN", token)
                                .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(actual)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(JsonUtil.serialize(Result.fail(ResponseCode.INVALID_USER_TOKEN, "Invalid user token!"))));
        mockMvc.perform(
                        get("/user/addUser")
                                .header("USER-TOKEN", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(JsonUtil.serialize(Result.fail(ResponseCode.INVALID_USER_TOKEN, "Invalid user token!"))));
    }

    @Test
    public void shouldNoticeContactAdmin() throws Exception {
        insertOne();
        User actual = copyFrom(BASE_USER);
        actual.setUserId(1L);
        String token = Base64Util.encode(JsonUtil.serialize(actual));
        mockMvc.perform(
                        get("/user/addUser")
                                .header("USER-TOKEN", token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(JsonUtil.serialize(Result.fail(ResponseCode.NO_ACCESS_FOR_RESOURCE, "You have no access for any resource! Please contact your admin"))));
    }

    @AfterEach
    public void resetData() throws JsonProcessingException {
        userDataDao.cleanDataWhenTest();
    }


}

