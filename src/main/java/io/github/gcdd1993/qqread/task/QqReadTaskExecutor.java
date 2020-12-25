package io.github.gcdd1993.qqread.task;

import io.github.gcdd1993.qqread.jpush.JPush;
import io.github.gcdd1993.qqread.jpush.WxServerPushImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "qqread")
public class QqReadTaskExecutor {

    @NestedConfigurationProperty
    @Getter
    @Setter
    private QqReadConfig[] configs;

    @Getter
    @Setter
    private String pushKey;

    private JPush jPush;

    private final List<QqReadTask> taskList = new LinkedList<>();

    @PostConstruct
    void init() {
        log.info("配置了{}个账号", configs.length);
        for (var config : configs) {
            var task = new QqReadTask(config);
            // test
            try {
                task.track();
                taskList.add(task);
                log.info("账号 {} 注册任务成功", config.getQq());
            } catch (QqReadCallException ex) {
                log.error("账号 {} 出现异常，请检查", config.getQq());
            }
        }
        this.jPush = new WxServerPushImpl("sc.ftqq.com", pushKey);
        log.info("Server酱推送初始化成功");

        //
        this.dailyNotify();
    }

    @Scheduled(cron = "${qqread.cron.daily-task}")
    void dailyTask() {
        taskList.forEach(QqReadTask::dailyTask);
    }

    @Scheduled(cron = "${qqread.cron.cycle-task}")
    void cycleTask() {
        taskList.forEach(QqReadTask::cycleTask);
    }

    @Scheduled(cron = "${qqread.cron.add-read-time}")
    void addReadTime() {
        taskList.forEach(QqReadTask::addReadTime);
    }

    @Scheduled(cron = "${qqread.cron.open-box}")
    void openBox() {
        taskList.forEach(QqReadTask::openBox);
    }

    @Scheduled(cron = "${qqread.cron.withdraw}")
    void withdraw() {
        taskList.forEach(QqReadTask::withdraw);
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
