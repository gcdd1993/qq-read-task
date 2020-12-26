package io.github.gcdd1993.qqread.task;

import io.github.gcdd1993.qqread.jpush.JPush;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("dev")
class QqReadTaskTest {

    @Autowired
    private QqReadTaskExecutor qqReadTaskExecutor;

    @Autowired
    private JPush jPush;

    @Test
    void dailyTask() {
        Arrays.stream(qqReadTaskExecutor.getConfigs())
                .map(it -> new QqReadTask(jPush, it))
                .forEach(QqReadTask::dailyTask);
    }

    @Test
    void cycleTask() {
        var task = new QqReadTask(jPush, qqReadTaskExecutor.getConfigs()[0]);
        task.cycleTask();
    }
}