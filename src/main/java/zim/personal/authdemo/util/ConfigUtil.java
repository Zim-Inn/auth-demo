package zim.personal.authdemo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ConfigUtil {

    private static Environment environment;

    @Autowired
    public void setEnvironment(Environment environment) {
        ConfigUtil.environment = environment;
    }

    public static String getProperty(String key) {
        return ConfigUtil.environment.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return ConfigUtil.environment.getProperty(key, defaultValue);
    }
}

