package io.github.gcdd1993.qqread;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@Getter
@Setter
public class QqReadConfig {
    private String qq;
    private String ywsession;
    private String cookie;
    private String body;
    private String bookUrl;
    private int maxDailyReadTime = 600;

    public TrackBody getTrackBody() {
        return JSON.parseObject(body, TrackBody.class);
    }
}
