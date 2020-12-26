package io.github.gcdd1993.qqread.config;

import io.github.gcdd1993.qqread.jpush.JPush;
import io.github.gcdd1993.qqread.jpush.WxServerPushImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gcdd1993
 * @date 2020/12/26
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "qqread.push")
public class JPushConfiguration {

    private String scKey;

    @Bean
    public JPush jPush() {
        var jPush = new WxServerPushImpl("sc.ftqq.com", scKey);
        log.info("Server酱推送初始化成功");
        return jPush;
    }
}
