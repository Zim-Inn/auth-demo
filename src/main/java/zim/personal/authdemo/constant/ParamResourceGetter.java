package zim.personal.authdemo.constant;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * {@link zim.personal.authdemo.aspect.annotation.AuthInfo}
 * define the func that to get in-touching resource list from request param
 */
@Getter
public enum ParamResourceGetter {
    // you should use EMPTY func when your AuthType is COMMON_USER
    EMPTY(null), FIRST_SINGLE_STRING_PARAM(l -> Collections.singletonList((String) l[0]));

    ParamResourceGetter(Function<Object[], List<String>> func) {
        this.func = func;
    }

    private final Function<Object[], List<String>> func;

}
