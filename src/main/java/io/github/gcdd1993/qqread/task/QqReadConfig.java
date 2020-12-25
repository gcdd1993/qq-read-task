package io.github.gcdd1993.qqread.task;

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
    private String bookUrl;
    private int maxDailyReadTime = 600;

    private String openid;
    private String guid;
    private String session;
    private int scene;
    private Object source;

    public TrackBody getTrackBody() {
        var trackBody = new TrackBody();
        trackBody.getCommon().setOpenid(this.openid);
        trackBody.getCommon().setGuid(this.guid);
        trackBody.getCommon().setSession(this.session);
        trackBody.getCommon().setScene(this.scene);
        trackBody.getCommon().setSource(this.source);
        return trackBody;
    }
}
