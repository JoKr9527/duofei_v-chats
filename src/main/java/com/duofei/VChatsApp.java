package com.duofei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 启动类
 * @author duofei
 * @date 2019/8/16
 */
@SpringBootApplication
@EnableWebMvc
public class VChatsApp {

    public static void main(String[] args) {
        SpringApplication.run(VChatsApp.class, args);
    }
}
