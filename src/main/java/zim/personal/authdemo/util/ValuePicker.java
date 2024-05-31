package zim.personal.authdemo.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * &#064;Author  Zim
 */
@Slf4j
public class ValuePicker {
    public static <T> T pickOrDef(T prefer, T def) {
        return usePreferOrDefault(prefer, Objects::isNull, def);
    }

    public static String pickStringOrDef(String prefer, String def) {
        return usePreferOrDefault(prefer, CheckBlankUtil::isBlank, def);
    }

    public static <T> T usePreferOrDefault(T prefer, Predicate<T> abandon, T def) {
        if (abandon == null) {
            throw new IllegalArgumentException("the abandon predicate must not be null");
        }
        if (!abandon.test(prefer)) {
            return prefer;
        } else {
            return def;
        }
    }

    public static String pickStringOrLazyDef(String prefer, Supplier<String> def) {
        return pickAndTestOrLazyDef(prefer, CheckBlankUtil::isBlank, def);
    }

    /**
     * @param prefer  优先选用
     * @param abandon 淘汰策略
     * @param def     兜底值
     * @param <T>     类型
     * @return 实例
     */
    public static <T> T pickAndTestOrLazyDef(T prefer, Predicate<T> abandon, Supplier<T> def) {
        if (abandon == null) {
            throw new IllegalArgumentException("the abandon predicate must not be null");
        }
        if (!abandon.test(prefer)) {
            return prefer;
        } else {
            try {
                return def.get();
            } catch (Exception e) {
                log.error("try to get default value failed, null will be returned:", e);
                return null;
            }
        }
    }

    public static <T> T pickOrLazyDef(T prefer, Supplier<T> def) {
        return pickAndTestOrLazyDef(prefer, Objects::isNull, def);
    }

    public static <T> T tryGetOrLazyDef(Supplier<T> prefer, Supplier<T> def) {
        T v;
        try {
            v = prefer.get();
        } catch (Exception e) {
            try {
                T defaultValue = def.get();
                log.info("the defaultValue {} will be use due to an exception", defaultValue);
                return defaultValue;
            } catch (Exception ex) {
                log.error("try to get default value failed, null will be returned:", ex);
                return null;
            }
        }
        return v == null ? def.get() : v;
    }

    public static <T> T tryGetOrDef(Supplier<T> prefer, T def) {
        T v;
        try {
            v = prefer.get();
        } catch (Exception e) {
            log.info("the defaultValue {} will be use due to an exception", def);
            return def;
        }
        return v == null ? def : v;
    }
}
