package zim.personal.authdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.Ordered;
import zim.personal.authdemo.config.DataProperties;
import zim.personal.authdemo.domain.User;
import zim.personal.authdemo.util.UserDataDao;

import java.util.Map;
import java.util.Objects;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = {DataProperties.class})
public class AuthDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthDemoApplication.class, args);
    }


}
