package io.github.gcdd1993.qqread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@EnableScheduling
@SpringBootApplication
public class QqReadApplication {
    public static void main(String[] args) {
        SpringApplication.run(QqReadApplication.class, args);
    }
}
