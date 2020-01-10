package cc.mrbird.demo.domain;

import lombok.*;
import org.springframework.stereotype.Component;

/**
 * @author MrBird
 */
@ToString
//@AllArgsConstructor
@NoArgsConstructor
@Data
// @Component
public class User {
    private String name;
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }


}
