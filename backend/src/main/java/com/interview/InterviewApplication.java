package com.interview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 智慧面试评分系统启动类
 */
@SpringBootApplication
@MapperScan("com.interview.mapper")
@EnableTransactionManagement
@EnableScheduling
public class InterviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewApplication.class, args);
        System.out.println("====================================");
        System.out.println("  智慧面试评分系统启动成功！");
        System.out.println("  API文档: http://localhost:8000/api/swagger-ui.html");
        System.out.println("====================================");
    }
}
