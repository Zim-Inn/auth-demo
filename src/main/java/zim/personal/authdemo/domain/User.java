package zim.personal.authdemo.domain;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private Long userId;
    private String role;
    private String accountName;
    private List<String> endpoint;
}
