package io.yggdrasil.labs.mealmate.start;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * Spring Boot Starter
 *
 * @author YoungerYang-Y
 */
@SpringBootApplication(scanBasePackages = {"io.yggdrasil.labs.mealmate", "com.alibaba.cola"})
@MapperScan(
        basePackages = "io.yggdrasil.labs.mealmate.infrastructure.persistence",
        markerInterface = BaseMapper.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
