package io.github.gcdd1993.qqread.task;

import io.github.gcdd1993.qqread.jpush.JPush;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "qqread")
public class QqReadTaskExecutor {

    @NestedConfigurationProperty
    private QqReadConfig[] configs;

    @Autowired
    private JPush jPush;

    private final List<QqReadTask> taskList = new LinkedList<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostConstruct
    void init() {
        log.info("配置了{}个账号", configs.length);
        for (var config : configs) {
            var task = new QqReadTask(jPush, config);
            // test
            try {
                task.track();
                taskList.add(task);
                log.info("账号 {} 注册任务成功", config.getQq());
            } catch (QqReadCallException ex) {
                log.error("账号 {} 出现异常，请检查", config.getQq());
            }
        }
    }

    @Scheduled(cron = "${qqread.cron.daily-task}")
    void dailyTask() {
        taskList.forEach(it -> executorService.submit(it::dailyTask));
    }

    @Scheduled(cron = "${qqread.cron.cycle-task}")
    void cycleTask() {
        taskList.forEach(it -> executorService.submit(it::cycleTask));
    }

    @Scheduled(cron = "${qqread.cron.add-read-time}")
    void addReadTime() {
        taskList.forEach(it -> executorService.submit(it::addReadTime));
    }

    @Scheduled(cron = "${qqread.cron.open-box}")
    void openBox() {
        taskList.forEach(it -> executorService.submit(it::openBox));
    }

    @Scheduled(cron = "${qqread.cron.withdraw}")
    void withdraw() {
        taskList.forEach(it -> executorService.submit(it::withdraw));
    }

    @Scheduled(cron = "${qqread.cron.notify}")
    void dailyNotify() {
        var notifyInfoList = taskList.stream().map(QqReadTask::dailyNotify).collect(Collectors.toList());
        // 统计余额
        var amount = notifyInfoList
                .stream()
                .map(NotifyInfo::getAmount)
                .reduce(0, Integer::sum);
        jPush.push("今日总收益", amount / 10_000 + "元");
        // 统计余额
        var balance = notifyInfoList
                .stream()
                .map(NotifyInfo::getBalance)
                .reduce(0, Integer::sum);
        jPush.push("账户总金额", balance / 10_000 + "元");
        jPush.push("账户详情", notifyInfoList.stream().map(Object::toString).collect(Collectors.joining("\n\n\n\n")));
    }

}
