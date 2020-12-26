package io.github.gcdd1993.qqread.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author gcdd1993
 * @date 2020/12/26
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("dev")
class QqReadTaskExecutorTest {

    @Autowired
    private QqReadTaskExecutor qqReadTaskExecutor;

    @Test
    void dailyNotify() {
        qqReadTaskExecutor.dailyNotify();
    }
}