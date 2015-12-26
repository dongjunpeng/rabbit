package com.buterfleoge.rabbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

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
@EntityScan(basePackages = "com.buterfleoge.whale.eo")
@EnableJpaRepositories("com.buterfleoge.whale.dao")
@EnableWebMvc
@ImportResource("applicationContext.xml")
public class Main extends WebMvcAutoConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}
