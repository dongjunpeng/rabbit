package com.buterfleoge.rabbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 
 * rabbit系统的入口
 * 
 * @author xiezhenzong
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan("com.buterfleoge")
@EntityScan(basePackages = "com.buterfleoge.whale.type.entity")
@EnableJpaRepositories("com.buterfleoge.whale.dao")
public class Main extends WebMvcAutoConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}
