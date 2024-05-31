package zim.personal.authdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import zim.personal.authdemo.config.DataProperties;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = {DataProperties.class})
public class AuthDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthDemoApplication.class, args);
    }


}
