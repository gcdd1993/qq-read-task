package io.github.gcdd1993.qqread;

import lombok.Data;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@Data
public class TrackBody {

    private Common common;
    private Data[] dataList;

    @lombok.Data
    public static class Common {
        private float appid;
        private float areaid;
        private String qq_ver;
        private String os_ver;
        private String mp_ver;
        private String mpos_ver;
        private String brand;
        private String model;
        private float screenWidth;
        private float screenHeight;
        private float windowWidth;
        private float windowHeight;
        private String openid;
        private float guid;
        private String session;
        private float scene;
        private String source;
        private String hasRedDot;
        private float missions;
        private float caseID;
    }

    @lombok.Data
    public static class Data {
        private String click1;
        private String click2;
        private String route;
        private String refer;
        Options options;
        private long dis;
        private float ext6;
        private String eventID;
        private String type;
        private float ccid;
        private String bid;
        private float bookStatus;
        private float bookPay;
        private float chapterStatus;
        Ext1 ext1;
        private String from;

        public long getDis() {
            return System.currentTimeMillis();
        }
    }

    @lombok.Data
    public static class Options {
        private String bid;
        private String cid;
        private String from;
    }

    @lombok.Data
    public static class Ext1 {
        private float font;
        private float bg;
        private float pageMode;
    }
}
