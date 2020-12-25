package io.github.gcdd1993.qqread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Optional;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@Slf4j
public class QqReadTask {
    private static final String HOST = "mqqapi.reader.qq.com";
    private static final String UA = "QQ/8.4.17.638 CFNetwork/1206 Darwin/20.1.0";
    private final OkHttpClient.Builder clientBuilder;
    private final Request.Builder requestBuilder;
    private final QqReadConfig qqReadConfig;
    private String nickname;
    private int[] readTimes = {60, 180, 360, 600, 900, 1200, 1500};

    public QqReadTask(QqReadConfig config) {
        this.qqReadConfig = config;
        this.clientBuilder = new OkHttpClient.Builder()
                .cookieJar(new OverlayCookieJar(HOST, config.getCookie()));
        this.requestBuilder = new Request.Builder()
                .addHeader("ywsession", this.qqReadConfig.getYwsession())
                .addHeader("Cookie", this.qqReadConfig.getCookie())
                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*")
                .addHeader("Host", HOST)
                .addHeader("User-Agent", UA)
                .addHeader("Referer", "https://appservice.qq.com/1110657249/0.30.0/page-frame.html")
                .addHeader("Accept-Language", "zh-cn")
//                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("mpversion", "0.30.0");
        ;
    }

    /**
     * 获取用户信息
     */
    private Optional<JSONObject> _getUserInfo() {
        return _getData("https://mqqapi.reader.qq.com/mqq/user/init");
    }

    /**
     * 阅豆签到
     */
    private Optional<JSONObject> _getDailyBeans() {
        return _postData("https://mqqapi.reader.qq.com/mqq/sign_in/user");
    }

    /**
     * 获取今日任务列表
     */
    private Optional<JSONObject> _getDailyTasks() {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/page?fromGuid=");
    }

    /**
     * 今日阅读时长
     */
    private Optional<JSONObject> _getDailyReadTime() {
        return _getData("https://mqqapi.reader.qq.com/mqq/page/config?router=%2Fpages%2Fbook-read%2Findex&options=");
    }

    /**
     * 阅读奖励，好像一个号只能领一次
     */
    private Optional<JSONObject> _getDailyReadTime(int seconds) {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/read_time_reward?seconds=" + seconds);
    }

    /**
     * 周阅读时长
     */
    private Optional<JSONObject> _getWeekReadTime() {
        return _getData("https://mqqapi.reader.qq.com/mqq/v1/bookShelfInit");
    }

    /**
     * 立即阅读
     */
    private Optional<JSONObject> _readWow() {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/read_book");
    }

    /**
     * 今日阅读时长
     */
    private Optional<JSONObject> _readTasks(int seconds) {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/read_time?seconds=" + seconds);
    }

    /**
     * 今日阅读时长
     */
    private Optional<JSONObject> _dailySign() {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/clock_in/page");
    }

    /**
     * 今日打卡看广告翻倍
     */
    private Optional<JSONObject> _watchDailySignAds() {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/clock_in_video");
    }

    /**
     * 看视频，拿金币
     */
    private Optional<JSONObject> _watchVideo() {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/watch_video");
    }

    /**
     * 每20分钟开一次宝箱
     */
    private Optional<JSONObject> _treasureBox() {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/treasure_box");
    }

    /**
     * 看广告，宝箱奖励翻倍
     */
    private Optional<JSONObject> _treasureBoxAds() {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/treasure_box_video");
    }

    /**
     * 周阅读奖励查询
     */
    private Optional<JSONObject> _getWeekReadTasks() {
        return _getData("https://mqqapi.reader.qq.com/mqq/pickPackageInit");
    }

    /**
     * 领取周阅读奖励
     *
     * @param readTime 阅读时长
     */
    private Optional<JSONObject> _getWeekReadTasks(int readTime) {
        return _getData("https://mqqapi.reader.qq.com/mqq/pickPackage?readTime=" + readTime);
    }

    /**
     * 刷时长
     *
     * @param uploadTime 阅读时长
     */
    private Optional<JSONObject> _readBooks(String bookUrl, int uploadTime) {
        // fixme 设置上传时长
        var url = bookUrl;
        return _getData(url);
    }

    /**
     * 数据追踪，解决1金币问题
     */
    private Optional<JSONObject> _track() {
        // fixme 设置上传时长
        var url = "https://mqqapi.reader.qq.com/log/v4/mqq/track";
        return _getData(url);
    }

    /**
     * 今日金币统计
     */
    private Optional<JSONObject> _getRedPackets(String pn) {
        var url = "https://mqqapi.reader.qq.com/mqq/red_packet/user/trans/list?pn=" + pn;
        return _getData(url);
    }

    /**
     * 提现
     */
    private Optional<JSONObject> _getWithdrawInfo(String pn) {
        var url = "https://mqqapi.reader.qq.com/mqq/red_packet/user/withdraw/page";
        return _getData(url);
    }

    /**
     * 提现
     */
    private Optional<JSONObject> _withdrawToWallet(String amount) {
        var url = "https://mqqapi.reader.qq.com/mqq/red_packet/user/withdraw?amount=" + amount;
        return _getData(url);
    }

    private Optional<JSONObject> _getData(String url) {
        var req = requestBuilder
                .url(url)
                .method("GET", null)
                .build();
        return _execute(req);
    }

    private Optional<JSONObject> _postData(String url) {
        var req = requestBuilder
                .url(url)
                .method("POST", RequestBody.Companion.create(new byte[0], null))
                .build();
        return _execute(req);
    }

    private Optional<JSONObject> _execute(Request request) {
        try {
            var res = this.clientBuilder
                    .build()
                    .newCall(request)
                    .execute();
            if (res.isSuccessful() && res.body() != null) {
                var json = JSON.parseObject(res.body().string());
                if (json.getInteger("code") == 0) {
                    return Optional.of(json.getJSONObject("data"));
                } else {
                    log.warn("get data error, url --> {}, msg --> {}", request.url().toString(), json.getString("msg"));
                }
                return Optional.empty();
            } else {
                log.error("get data error, url --> {}, code --> {}", request.url().toString(), res.code());
            }
        } catch (IOException e) {
            log.error("get data error, url --> {}", request.url().toString(), e);
        }
        return Optional.empty();
    }

}
