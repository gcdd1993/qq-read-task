package io.github.gcdd1993.qqread.task;

import lombok.Data;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@Data
public class TrackBody {

    private Common common = new Common();
    private Data[] dataList = new Data[]{
            new Data()
    };

    @lombok.Data
    public static class Common {
        private int appid = 1450024393;
        private int areaid = 5;
        private String qq_ver = "8.3.9.2952";
        private String os_ver = "Android 10";
        private String mp_ver = "0.33.3";
        private String mpos_ver = "1.16.0";
        private String brand = "Xiaomi";
        private String model = "MIX 2S";
        private int screenWidth = 393;
        private int screenHeight = 738;
        private int windowWidth = 393;
        private int windowHeight = 684;
        private String openid;
        private String guid;
        private String session;
        private int scene;
        private Object source;
        private String hasRedDot = "false";
        private int missions = -1;
        private int caseID = -1;
    }

    @lombok.Data
    public static class Data {
        private String click1 = "bookShelf_myshelf_myBook_C";
        private String click2 = "promoPopwin_close_C";
        private String route = "pages/book-read/index";
        private String refer = "pages/book-shelf/index";
        Options options = new Options();
        private long dis = System.currentTimeMillis();
        private int ext6 = 33;
        private String eventID = "bookRead_show_I";
        private String type = "shown";
        private int ccid = 7;
        private String bid = "319700";
        private int bookStatus = 1;
        private int bookPay = 0;
        private int chapterStatus = 0;
        Ext1 ext1 = new Ext1();
        private String from = "bookShelf_myshelf_myBook_C_0_319700";
    }

    @lombok.Data
    public static class Options {
        private String bid = "319700";
        private String cid = "7";
        private String from = "shelf";
    }

    @lombok.Data
    public static class Ext1 {
        private int font = 18;
        private int bg = 1;
        private int pageMode = 1;
    }
}
