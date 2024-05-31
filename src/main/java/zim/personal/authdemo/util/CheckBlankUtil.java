package zim.personal.authdemo.util;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * &#064;Author  Zim
 */
public class CheckBlankUtil {
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        return str.trim().isEmpty();
    }

    public static boolean anyMatchBlank(Object... targets) {
        return !noneMatchBlank(targets);
    }

    public static boolean allMatchBlank(Object... targets) {
        return !anyMatchNotBlank(targets);
    }

    public static boolean anyMatchNotBlank(Object... targets) {
        if (targets == null || targets.length == 0) {
            return false;
        }
        for (Object target : targets) {
            if (null == target) {
                continue;
            }
            if (target instanceof String) {
                if (isBlank((String) target)) {
                    continue;
                }
                return true;
            }
            Class<?> clazz = target.getClass();
            if (Collection.class.isAssignableFrom(clazz)) {
                if (anyMatchNotBlank(((Collection<?>) target).toArray())) {
                    return true;
                }
            } else if (clazz.isArray()) {
                int l = Array.getLength(target);
                if (l == 0) {
                    continue;
                }
                Object firstOne = Array.get(target, 0);
                if (firstOne != null && firstOne.getClass().isArray()) {
                    for (int i = 0; i < l; i++) {
                        if (anyMatchNotBlank((Object[]) Array.get(target, 0))) {
                            return true;
                        }
                    }
                } else {
                    Class<?> componentType = clazz.getComponentType();
                    if (componentType.isPrimitive()) {
                        for (int i = 0; i < l; i++) {
                            if (null != Array.get(target, i)) {
                                return true;
                            }
                        }
                    }
                    if (anyMatchNotBlank((Object[]) target)) {
                        return true;
                    }
                }
            } else if (target instanceof Number) {
                return true;
            }
        }
        return false;
    }

    public static boolean noneMatchBlank(Object... targets) {
        if (targets == null || targets.length == 0) {
            return false;
        }
        for (Object target : targets) {
            if (null == target) {
                return false;
            }
            if (target instanceof String) {
                if (isBlank((String) target)) {
                    return false;
                }
                continue;
            }
            Class<?> clazz = target.getClass();
            if (clazz.isArray()) {
                int l = Array.getLength(target);
                if (l == 0) {
                    return false;
                }
                Object firstOne = Array.get(target, 0);
                if (firstOne != null && firstOne.getClass().isArray()) {
                    for (int i = 0; i < l; i++) {
                        if (!noneMatchBlank((Object[]) Array.get(target, 0))) {
                            return false;
                        }
                    }
                } else {
                    /*
                     *  allNotBlankString所接受的不定参数构成一个Object[]传入方法,
                     *  因此入参(obj1,obj2)与入参(new Object[]{obj1,obj2})是等价的
                     *  但如果直接传入一个TypeExtendsObject[] thisArray,则只相当于new Object[]{thisArray},在递归调用时会导致栈溢出问题
                     *  而基本类型数组如int[]不能向上转向为为Object[],所以当判断其为基本类型数组,只需排除数组元素为空的情况即可
                     */
                    Class<?> componentType = clazz.getComponentType();
                    if (componentType.isPrimitive()) {
                        for (int i = 0; i < l; i++) {
                            if (null == Array.get(target, i)) {
                                return false;
                            }
                        }
                        continue;
                    }
                    if (!noneMatchBlank((Object[]) target)) {
                        return false;
                    }
                }
                continue;
            } else if (Collection.class.isAssignableFrom(clazz)) {
                if (noneMatchBlank(((Collection<?>) target).toArray())) {
                    continue;
                } else {
                    return false;
                }
            } else if (target instanceof Number) {
                continue;
            }
            return false;
        }
        return true;
    }
}
