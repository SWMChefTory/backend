package com.cheftory.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ChefTory API 서비스의 메인 애플리케이션 클래스.
 *
 * <p>Spring Boot 애플리케이션을 시작하고 REST API 서비스를 제공합니다.</p>
 */
@EnableAsync
@SpringBootApplication
@EnableScheduling
public class ApiApplication {
    /**
     * 애플리케이션을 시작합니다.
     *
     * @param args 커맨드 라인 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
