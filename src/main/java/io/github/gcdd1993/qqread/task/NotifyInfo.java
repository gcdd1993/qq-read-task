package io.github.gcdd1993.qqread.task;

import lombok.Data;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@Data
public class NotifyInfo {
    private String qq;
    private String nickName;
    /**
     * 今日收益
     */
    private int amount;
    private String amountStr;

    /**
     * 余额
     */
    private int balance;
    private String balanceStr;

    private String dayRead;
    private String weekRead;

    /**
     * 今日开启宝箱数
     */
    private int boxes;

    @Override
    public String toString() {
        return "NotifyInfo{" +
                "qq='" + qq + '\'' +
                ", 昵称='" + nickName + '\'' +
                ", 今日收益=" + amount +
                ", 今日收益='" + amountStr + '\'' +
                ", 余额=" + balance +
                ", 余额='" + balanceStr + '\'' +
                ", 今日阅读='" + dayRead + '\'' +
                ", 周阅读='" + weekRead + '\'' +
                ", 今日开启宝箱=" + boxes +
                '}';
    }
}
