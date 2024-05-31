package zim.personal.authdemo.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import zim.personal.authdemo.aspect.annotation.AuthInfo;
import zim.personal.authdemo.constant.ResponseCode;
import zim.personal.authdemo.domain.User;
import zim.personal.authdemo.exception.CustomRuntimeException;
import zim.personal.authdemo.util.Base64Util;
import zim.personal.authdemo.util.CheckBlankUtil;
import zim.personal.authdemo.util.JsonUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
@Slf4j
public class AuthAspect {

    @Pointcut("execution(* zim.personal.authdemo.*.controller..*(..))")
    private void requestAuthIntercept() {
    }

    @Around("requestAuthIntercept()")
    public Object authIntercept(ProceedingJoinPoint joinPoint) throws Throwable {
        StringBuffer param = new StringBuffer();
        String className = joinPoint.getTarget().getClass().getName();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSignature.getMethod().getName();
        Object[] args = joinPoint.getArgs();
        Object result;
        logging(args, param, className, methodName);
        authing(methodSignature.getMethod(), args);
        result = joinPoint.proceed();
        return result;
    }

    // it can be also expand to check other resources
    private static void authing(Method method, Object[] param) throws JsonProcessingException {
        AuthInfo authInfo = method.getAnnotation(AuthInfo.class);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        User user;
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            String encodedHeaderValue = request.getHeader("USER-TOKEN");
            String decodedJson = Base64Util.decode(encodedHeaderValue);
            user = JsonUtil.deserialize(decodedJson, User.class);
            if (CheckBlankUtil.anyMatchBlank(user.getUserId(), user.getRole())) {
                throw new CustomRuntimeException("couldn't find auth in header", ResponseCode.INVALID_REQUEST);
            }
        } else {
            throw new CustomRuntimeException("couldn't find auth in header", ResponseCode.INVALID_REQUEST);
        }
        switch (authInfo.type()) {
            case JUST_PASS:
                return;
            case ONLY_ADMIN:
                if ("admin".equals(user.getRole())) {
                    return;
                }
                throw new CustomRuntimeException("couldn't find auth in header", ResponseCode.REQUIRE_ADMIN);

            case COMMON_USER:
                List<String> availableEndpoints = user.getEndpoint();
                if (availableEndpoints != null && !availableEndpoints.isEmpty()) {
                    List<String> requestEndpoints = authInfo.funcToResourcesList().getFunc().apply(param);
                    if (requestEndpoints == null) {
                        log.error("Expect at least one string in param but get null, please let developers to check the func.\n" + "The problem may be in the method : {}", method.getName());
                        throw new CustomRuntimeException("invalid param", ResponseCode.INVALID_REQUEST);
                    }
                    Optional<String> beyondEndpoints = requestEndpoints.stream().filter(l -> !availableEndpoints.contains(l)).findAny();
                    if (beyondEndpoints.isPresent()) {
                        log.warn("Found a request that exceed his authority! The userInfo is {}, and its request endpoints are {}", user, requestEndpoints);
                        throw new CustomRuntimeException("You have no access for :" + beyondEndpoints.get(), ResponseCode.NO_ACCESS_FOR_RESOURCE);
                    }
                }
        }
    }

    private static void logging(Object[] args, StringBuffer param, String className, String methodName) throws JsonProcessingException {
        for (Object o : args) {
            if (!(o instanceof ServletRequest) && !(o instanceof ServletResponse) && !(o instanceof MultipartFile)) {
                param.append(String.format("%s", JsonUtil.serialize(o)));
            }
        }
        log.info("{} {} request：{}", className, methodName, param);
    }


}
