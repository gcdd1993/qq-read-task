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
    private TrackBody body;
    private String bookUrl;
    private int maxDailyReadTime;

    /**
     * 默认不启用提现功能，启用前必须保证账号已经实名认证（模式：满额提现，每天23点以后开始尝试提现，每次提现10元，不满10元不提现，不使用2元和1元提现次数）
     */
    private boolean withdraw = true;

    /**
     * 默认不启用托管提现，若启用则必须在 WITHDRAW 为 true 下才有效（模式：托管提现，建议放弃薅此羊毛时候打开，将使用剩余的2元和1元提现次数将剩余金币最大程度转化为rmb）
     */
    private boolean hostingMode;

    public void setBody(String trackBody) {
        this.body = JSON.parseObject(trackBody, TrackBody.class);
    }
}
