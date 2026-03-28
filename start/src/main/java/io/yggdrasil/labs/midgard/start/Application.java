package io.yggdrasil.labs.midgard.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Starter
 *
 * @author YoungerYang-Y
 */
@SpringBootApplication(scanBasePackages = {"io.yggdrasil.labs.midgard", "com.alibaba.cola"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
