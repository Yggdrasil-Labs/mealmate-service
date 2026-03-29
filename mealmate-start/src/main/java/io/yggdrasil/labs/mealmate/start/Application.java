package io.yggdrasil.labs.mealmate.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Starter
 *
 * @author YoungerYang-Y
 */
@SpringBootApplication(scanBasePackages = {"io.yggdrasil.labs.mealmate", "com.alibaba.cola"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
