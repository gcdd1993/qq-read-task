package io.github.gcdd1993.qqread.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.gcdd1993.qqread.jpush.JPush;
import io.github.gcdd1993.qqread.util.Utils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@Slf4j
public class QqReadTask {
    private static final String HOST = "mqqapi.reader.qq.com";
    private static final String UA = "QQ/8.4.17.638 CFNetwork/1206 Darwin/20.1.0";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");
    private final OkHttpClient.Builder clientBuilder;
    private final Request.Builder requestBuilder;
    private final QqReadConfig qqReadConfig;
    private final JPush jPush;
    private static final int WITHDRAW_1 = 10_000; // 1元
    private static final int WITHDRAW_0_6 = 6_000; // 0.6元
    private static final int WITHDRAW_10 = WITHDRAW_1 * 10; // 10元

    public QqReadTask(JPush jPush, QqReadConfig config) {
        this.jPush = jPush;
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
    }

    /**
     * 每日任务，每天前一小时，每隔5分钟运行一次
     * <p>
     * 请先完成「立即阅读任务」后，解锁今日任务
     */
    public void dailyTask() {
        track();
        // 今日打卡
        _getDailyTasks()
                .ifPresent(tasks -> {
                    // 立即阅读《xxxx》
                    _findByTaskName("立即阅读", tasks.getJSONArray("taskList"))
                            .ifPresent(task -> {
                                var enableFlag = task.getInteger("enableFlag");
                                var doneFlag = task.getInteger("doneFlag");
                                if (enableFlag == 1 && doneFlag == 0) {
                                    _readNow()
                                            .ifPresent(readNowReward -> {
                                                _i("【{}】获得{}金币", task.getString("title"), readNowReward.getInteger("amount"));
//                                                this.jPush.push(MessageFormat.format("账号[{0}]立即阅读完成", qqReadConfig.getQq()), readNowReward.toJSONString());
                                            });
                                } else {
                                    _w("【{}】今日已完成，请不要重复操作", task.getString("title"));
//                        this.jPush.push(MessageFormat.format("账号[{0}]立即阅读重复完成", qqReadConfig.getQq()), tasks.toJSONString());
                                }
                            });
                    // 今日打卡
                    _findByTaskName("今日打卡", tasks.getJSONArray("taskList"))
                            .ifPresent(task -> {
                                var enableFlag = task.getInteger("enableFlag");
                                var doneFlag = task.getInteger("doneFlag");
                                if (enableFlag == 1 && doneFlag == 0) {
                                    _dailySign()
                                            .ifPresent(dailySign -> {
                                                _i(
                                                        "【{}】获得{}金币",
                                                        task.getString("title"),
                                                        dailySign.getInteger("amount")
                                                );
                                                _sleep(15);
                                                var videoDoneFlag = dailySign.getInteger("videoDoneFlag");
                                                if (videoDoneFlag == 0) {
                                                    _watchDailySignAds()
                                                            .ifPresent(dailySignAds -> {
                                                                _i("【打卡翻倍】获得{}金币", dailySignAds.getInteger("amount"));
                                                            });
                                                }
                                            });
                                }
                            });
                });
        // 周时长奖励
        _getWeekReadTime()
                .ifPresent(weekReadTime -> {
                    var readTime = weekReadTime.getInteger("readTime");
                    _getWeekReadTasks()
                            .ifPresent(weekReadTasks -> {
                                for (int i = 0; i < weekReadTasks.size(); i++) {
                                    var a = weekReadTasks.getJSONObject(i);
                                    var readTimeT = a.getInteger("readTime");
                                    var isPick = a.getBoolean("isPick");
                                    if (readTime >= readTimeT && !isPick) {
                                        // 领取周时长奖励
                                        _getWeekReadReward(readTimeT);
                                        _i("【周时长奖励】领取{}时长奖励成功", readTimeT);
                                    }
                                }
                            });
                });
        // 阅豆签到
        _getDailyBeans()
                .ifPresent(__ -> _i("【阅豆签到】获得{}阅豆", __.getInteger("takeTicket")));
    }

    /**
     * 读书刷时长
     */
    public void addReadTime() {
        track();
        _getDailyReadTime()
                .ifPresent(dailyReadTime -> {
                    var time = dailyReadTime.getInteger("todayReadSeconds") / 60;
                    if (time >= qqReadConfig.getMaxDailyReadTime()) {
                        _i("【阅读时长】今日阅读时长{}已达到设置的最大阅读时长{}，故不增加阅读时长", time, qqReadConfig.getMaxDailyReadTime());
                    } else {
                        var uploadTime = Utils.randomInt(5 * 1000 * 60, 6 * 1000 * 60); //5分钟到6分钟之间
                        _readBooks(qqReadConfig.getBookUrl(), uploadTime);
                        _i("【阅读时长】成功增加{}分钟阅读时长", uploadTime / 60000);
                    }
                });
    }

    /**
     * 开宝箱领金币
     * <p>
     * 启动程序时，运行一次，然后根据宝箱时间，确认下次运行时间
     */
    public void openBox() {
        track();
        _getDailyTasks()
                .ifPresent(tasks -> {
                    var doneFlag = tasks.getJSONObject("treasureBox").getInteger("doneFlag");
                    if (doneFlag == 0) {
                        var box = _treasureBox();
                        if (box.isPresent()) {
                            _i("【开启第{}个宝箱】获得{}金币", box.get().getInteger("count"), box.get().getInteger("amount"));
                            _getDailyTasks()
                                    .ifPresent(tasks1 -> {
                                        var videoDoneFlag = tasks1.getJSONObject("treasureBox").getInteger("videoDoneFlag");
                                        if (videoDoneFlag == 0) {
                                            _treasureBoxAds()
                                                    .ifPresent(boxAds -> {
                                                        _i("【宝箱奖励翻倍】获得{}金币", boxAds.getInteger("amount"));
                                                    });
                                        } else {
                                            _w("【宝箱奖励翻倍】未到时间");
                                        }
                                    });
                        } else {
                            _w("【开启宝箱失败】");
                        }
                    } else {
                        var tip = tasks.getJSONObject("treasureBox").getString("tipText");
                        _d("【开启宝箱失败】未到开启宝箱时间，下一个宝箱{}", tip);
                    }
                });
    }

    /**
     * 1、立即阅读《xxx》
     * 2、阅读任务
     * 3、看视频领金币
     */
    public void cycleTask() {
        track();
        _getDailyTasks()
                .ifPresent(tasks -> {
                    // 阅读任务
                    _findByTaskName("阅读任务", tasks.getJSONArray("taskList"))
                            .ifPresent(task -> {
                                var enableFlag = task.getInteger("enableFlag");
                                var doneFlag = task.getInteger("doneFlag");
                                if (enableFlag == 1 && doneFlag == 0) {
                                    var config = task.getJSONArray("config");
                                    for (int i = 0; i < config.size(); i++) {
                                        var c = config.getJSONObject(i);
                                        var eFlag = c.getInteger("enableFlag");
                                        var dFlag = c.getInteger("doneFlag");
                                        if (eFlag == 1 && dFlag == 0) {
                                            var s = c.getInteger("seconds");
                                            _readTasks(s)
                                                    .ifPresent(readTaskReward -> {
                                                        _i("【阅读任务】阅读{}，获得{}金币", c.getString("timeStr"), readTaskReward.getInteger("amount"));
                                                    });
                                        }
                                    }
                                } else {
                                    _w("【阅读任务】今日已完成，请不要重复操作");
                                }
                            });
                    // 看视频领金币
                    _findByTaskName("看视频领金币", tasks.getJSONArray("taskList"))
                            .ifPresent(task -> {
                                var enableFlag = task.getInteger("enableFlag");
                                var doneFlag = task.getInteger("doneFlag");
                                if (enableFlag == 1 && doneFlag == 0) {
                                    var subTitle = task.getString("subTitle");
                                    var count = subTitle
                                            .replace("(", "")
                                            .replace(")", "")
                                            .split("/");
                                    var finish = Integer.parseInt(count[0]);
                                    var total = Integer.parseInt(count[1]);
                                    if (finish < total) {
                                        _watchVideo()
                                                .ifPresent(__ -> {
                                                    _i("【视频奖励】获得{}金币({}/{})", __.getInteger("amount"), finish + 1, total);
                                                });
                                    } else {
                                        _i("【视频奖励】今日任务已完成");
                                    }
                                } else {
                                    _w("【视频奖励】今日已完成，请不要重复操作");
                                }
                            });
                });
        // 阅读奖励
        _getDailyReadTime()
                .ifPresent(dailyReadTime -> {
                    var readTimeRewardTask = dailyReadTime.getJSONArray("readTimeRewardTask");
                    for (int i = 0; i < readTimeRewardTask.size(); i++) {
                        var readTimeTask = readTimeRewardTask.getJSONObject(i);
                        var enableFlag = readTimeTask.getInteger("enableFlag");
                        var doneFlag = readTimeTask.getInteger("doneFlag");
                        if (enableFlag == 1 && doneFlag == 0) {
                            var seconds = readTimeTask.getInteger("seconds");
                            _readTimeRewardTasks(seconds)
                                    .ifPresent(readTimeReward -> _i("【阅读奖励】阅读{}秒，获得金币{}", seconds, readTimeReward.getInteger("amount")));
                        }
                    }
                });
    }

    /**
     * 数据跟踪
     */
    public void track() {
        _track().ifPresent(__ -> _i("数据追踪成功"));
    }

    /**
     * 托管提现
     */
    public void withdraw() {
        _getUserInfo()
                .flatMap(user -> _getWithdrawInfo())
                .ifPresent(withdrawInfo -> {
                    var amount = withdrawInfo.getJSONObject("user").getInteger("amount");
                    var configList = withdrawInfo.getJSONArray("configList");
                    // 0.6元提现
                    if (configList.getJSONObject(0).getInteger("amount") == WITHDRAW_0_6) {
                        _withdrawToWallet(WITHDRAW_0_6);
                        _i("【托管提现】提现0.6元成功");
                        amount -= WITHDRAW_0_6;
                    }
                    // 满10就提现
                    if (amount >= WITHDRAW_10) {
                        // 满10块钱了
                        _withdrawToWallet(WITHDRAW_10);
                        _i("【托管提现】提现10元成功");
                    } else {
                        _i("【托管提现】余额{}不足，无法提现", amount);
                    }
                });
    }

    /**
     * 每日通知，建议在11点半运行
     */
    public NotifyInfo dailyNotify() {
        var notifyInfo = new NotifyInfo();
        notifyInfo.setQq(this.qqReadConfig.getQq());
        _getUserInfo()
                .ifPresent(it -> {
                    notifyInfo.setNickName(it.getJSONObject("user").getString("nickName"));
                });
        _getDailyTasks()
                .ifPresent(it -> {
                    var amount = it.getJSONObject("user").getInteger("amount");
                    notifyInfo.setBalance(amount);
                    notifyInfo.setBalanceStr(amount / 10_000 + "元");

                    notifyInfo.setBoxes(it.getJSONObject("treasureBox").getInteger("count"));
                });
        var amount = totalAward(LocalDate.now());
        notifyInfo.setAmount(amount);
        notifyInfo.setAmountStr(amount / 10_000 + "元");

        _getWeekReadTime()
                .ifPresent(it -> {
                    notifyInfo.setWeekRead(MessageFormat.format(
                            "{0}小时{1}分钟",
                            it.getInteger("readTime") / 60,
                            it.getInteger("readTime") % 60
                    ));
                });
        _getDailyReadTime()
                .ifPresent(it -> {
                    notifyInfo.setDayRead(MessageFormat.format(
                            "{0}小时{1}分钟",
                            it.getInteger("todayReadSeconds") / 3600,
                            it.getInteger("todayReadSeconds") / 60 % 60
                    ));
                });
        return notifyInfo;
    }

    /**
     * 查询收益
     *
     * @param date 日期
     * @return 收益
     */
    public int totalAward(LocalDate date) {
        // 定为15页，尽量只查询当天
        var ts0 = date.atStartOfDay().atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        var ts1 = date.plusDays(1L).atStartOfDay().atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        var amount = 0;
        for (int i = 1; i < 16; i++) {
            var arr = _getRedPackets(i)
                    .map(it -> it.getJSONArray("list"));
            if (arr.isPresent()) {
                for (int j = 0; j < arr.get().size(); j++) {
                    var award = arr.get().getJSONObject(j);
                    var createTime = award.getLong("createTime");
                    if (createTime < ts1 && createTime >= ts0) {
                        amount += award.getInteger("amount");
                    } else {
                        return amount;
                    }
                }
            }
        }
        return amount;
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
        return _getData("https://mqqapi.reader.qq.com/mqq/page/config?router=%2Fpages%2Fbook-read%2Findex&options=")
                .map(__ -> __.getJSONObject("pageParams"));
    }

    /**
     * 阅读奖励，好像一个号只能领一次
     */
    private Optional<JSONObject> _readTimeRewardTasks(int seconds) {
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
    private Optional<JSONObject> _readNow() {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/read_book");
    }

    /**
     * 每日阅读任务
     */
    private Optional<JSONObject> _readTasks(int seconds) {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/read_time?seconds=" + seconds);
    }

    /**
     * 今日打卡
     */
    private Optional<JSONObject> _dailySign() {
        return _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/clock_in");
    }

    /**
     * 今日打卡看广告翻倍
     */
    private Optional<JSONObject> _watchDailySignAds() {
        var res = _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/clock_in_video");
        _sleep(3);
        return res;
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
        var res = _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/treasure_box");
        _sleep(15);
        return res;
    }

    /**
     * 看广告，宝箱奖励翻倍
     */
    private Optional<JSONObject> _treasureBoxAds() {
        var res = _getData("https://mqqapi.reader.qq.com/mqq/red_packet/user/treasure_box_video");
        _sleep(15);
        return res;
    }

    /**
     * 周阅读奖励查询
     */
    private Optional<JSONArray> _getWeekReadTasks() {
        return _getDataArr("https://mqqapi.reader.qq.com/mqq/pickPackageInit");
    }

    /**
     * 领取周阅读奖励
     *
     * @param readTime 阅读时长
     */
    private Optional<JSONObject> _getWeekReadReward(int readTime) {
        return _getData("https://mqqapi.reader.qq.com/mqq/pickPackage?readTime=" + readTime);
    }

    /**
     * 刷时长
     *
     * @param uploadTime 上传时长 单位毫秒
     */
    private Optional<JSONObject> _readBooks(String bookUrl, int uploadTime) {
        var url = bookUrl.replaceAll("(.*)(readTime=.*&read_type=)(.*)", "$1readTime=" + uploadTime + "&read_type=$3");
        return _getData(url);
    }

    /**
     * 数据追踪，解决1金币问题
     */
    private Optional<JSONObject> _track() {
        // 替换dis 时间戳
        var requestBody = RequestBody.Companion.create(JSON.toJSONString(qqReadConfig.getTrackBody()), MEDIA_TYPE_JSON);
        var url = "https://mqqapi.reader.qq.com/log/v4/mqq/track";
        return _postData(url, requestBody);
    }

    /**
     * 今日金币统计
     *
     * @param pn 页码
     */
    private Optional<JSONObject> _getRedPackets(int pn) {
        var url = "https://mqqapi.reader.qq.com/mqq/red_packet/user/trans/list?pn=" + pn;
        return _getData(url);
    }

    /**
     * 提现
     */
    private Optional<JSONObject> _getWithdrawInfo() {
        var url = "https://mqqapi.reader.qq.com/mqq/red_packet/user/withdraw/page";
        return _getData(url);
    }

    /**
     * 提现
     */
    private Optional<JSONObject> _withdrawToWallet(int amount) {
        var url = "https://mqqapi.reader.qq.com/mqq/red_packet/user/withdraw?amount=" + amount;
        return _postData(url);
    }

    private Optional<JSONArray> _getDataArr(String url) {
        var req = requestBuilder
                .url(url)
                .method("GET", null)
                .build();
        return _execute(req, new JsonArrayDataFunc());
    }

    private Optional<JSONObject> _getData(String url) {
        var req = requestBuilder
                .url(url)
                .method("GET", null)
                .build();
        return _execute(req, new JsonObjectDataFunc());
    }

    private Optional<JSONObject> _postData(String url) {
        return _postData(url, RequestBody.Companion.create(new byte[0], null));
    }

    private Optional<JSONObject> _postData(String url, RequestBody body) {
        var req = requestBuilder
                .url(url)
                .method("POST", body)
                .build();
        return _execute(req, new JsonObjectDataFunc());
    }

    private <T> Optional<T> _execute(Request request, Function<JSONObject, T> dataFunc) {
        try {
            var res = this.clientBuilder
                    .build()
                    .newCall(request)
                    .execute();
            if (res.isSuccessful() && res.body() != null) {
                var json = JSON.parseObject(res.body().string());
                _d(json.toJSONString());
                _sleep(1);
                if (json.getInteger("code") == 0) {
//                    return Optional.ofNullable(json.getJSONObject("data"));
                    return Optional.ofNullable(dataFunc.apply(json));
                } else {
                    _w("get data error, url --> {}, msg --> {}", request.url().toString(), json.getString("msg"));
                    // 可知的异常处理
                    _handleError(json.getString("msg"));
                    throw new QqReadCallException(MessageFormat.format(
                            "在解析数据时出现错误 code --> {0}, msg --> {1}, url --> {2}",
                            json.getInteger("code"),
                            json.getString("msg"),
                            request.url().toString()
                    ));
                }
            } else {
                _e("get data error, url --> {}, code --> {}", request.url().toString(), res.code());
                throw new QqReadCallException(MessageFormat.format(
                        "在获取数据时出现错误 code --> {0}, url --> {1}",
                        res.code(),
                        request.url().toString()
                ));
            }
        } catch (IOException e) {
            _e("get data error, url --> {}", request.url().toString(), e);
            throw new QqReadCallException(MessageFormat.format("在调用接口时出现错误 {0} ", request.url().toString()));
        }
    }

    private void _sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            _e("", e);
        }
    }

    private void _i(String format, Object... arguments) {
        log.info("[{}] " + format, _mergeArgs(arguments));
    }

    private void _d(String format, Object... arguments) {
        log.debug("[{}] " + format, _mergeArgs(arguments));
    }

    private void _w(String format, Object... arguments) {
        log.warn("[{}] " + format, _mergeArgs(arguments));
    }

    private void _e(String format, Object... arguments) {
        log.error("[{}] " + format, _mergeArgs(arguments));
    }

    /**
     * 合并参数列表
     *
     * @param arguments 参数
     * @return
     */
    private Object[] _mergeArgs(Object... arguments) {
        var args = new Object[arguments.length + 1];
        if (arguments.length > 0) {
            System.arraycopy(arguments, 0, args, 1, arguments.length);
            args[0] = this.qqReadConfig.getQq();
        } else {
            args = new Object[]{this.qqReadConfig.getQq()};
        }
        return args;
    }

    /**
     * 从今日任务列表寻找任务
     *
     * @param name 任务名称
     * @return 任务
     */
    private Optional<JSONObject> _findByTaskName(String name, JSONArray taskList) {
        return taskList
                .stream()
                .filter(it -> {
                    var obj = (JSONObject) it;
                    var title = obj.getString("title");
                    return title.contains(name);
                })
                .map(JSONObject.class::cast)
                .findAny();
    }

    private static class JsonObjectDataFunc
            implements Function<JSONObject, JSONObject> {

        @Override
        public JSONObject apply(JSONObject jsonObject) {
            return jsonObject.getJSONObject("data");
        }
    }

    private static class JsonArrayDataFunc
            implements Function<JSONObject, JSONArray> {

        @Override
        public JSONArray apply(JSONObject jsonObject) {
            return jsonObject.getJSONArray("data");
        }
    }

    private void _handleError(String msg) {
        if (msg.contains("立即阅读任务")) {
            // 立即阅读《xxxx》
            _getDailyTasks()
                    .flatMap(tasks -> _findByTaskName("立即阅读", tasks.getJSONArray("taskList")))
                    .ifPresent(task -> {
                        var enableFlag = task.getInteger("enableFlag");
                        var doneFlag = task.getInteger("doneFlag");
                        if (enableFlag == 1 && doneFlag == 0) {
                            _readNow()
                                    .ifPresent(readNowReward -> {
                                        _i("【{}】获得{}金币", task.getString("title"), readNowReward.getInteger("amount"));
//                                                this.jPush.push(MessageFormat.format("账号[{0}]立即阅读完成", qqReadConfig.getQq()), readNowReward.toJSONString());
                                    });
                        } else {
                            _w("【{}】今日已完成，请不要重复操作", task.getString("title"));
//                        this.jPush.push(MessageFormat.format("账号[{0}]立即阅读重复完成", qqReadConfig.getQq()), tasks.toJSONString());
                        }
                    });
        }
    }

}
