package io.github.gcdd1993.qqread.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@SpringBootTest
//@ActiveProfiles("dev")
class QqReadTaskTest {

    @Autowired
    private QqReadTaskExecutor qqReadTaskExecutor;

    @Test
    void dailyTask() {
        Arrays.stream(qqReadTaskExecutor.getConfigs())
                .map(QqReadTask::new)
                .forEach(QqReadTask::dailyTask);
    }

    @Test
    void cycleTask() {
        var task = new QqReadTask(qqReadTaskExecutor.getConfigs()[0]);
        task.cycleTask();
    }
}