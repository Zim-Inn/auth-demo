package zim.personal.authdemo.aspect.annotation;

import zim.personal.authdemo.constant.ParamResourceGetter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthInfo {

    AuthType type() default AuthType.COMMON_USER;

    ParamResourceGetter funcToResourcesList() default ParamResourceGetter.EMPTY;
}
