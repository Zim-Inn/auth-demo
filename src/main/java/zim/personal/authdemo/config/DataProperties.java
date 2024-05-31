package zim.personal.authdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "data.path")
public class DataProperties {
    //
    String user;


//    String otherDomain;
}
