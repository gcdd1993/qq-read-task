package io.github.gcdd1993.qqread;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
class QqReadTaskTest {

    @Test
    void getUserInfo() throws IOException {
        var qqReadConfig = new QqReadConfig();
        qqReadConfig.setQq("1398371419");
        qqReadConfig.setYwsession("btges9uft294gal7uljxtxvb34ub8qjq");
        qqReadConfig.setCookie("ywguid=1398371419;ywkey=ywCD5k6WhEQi;platform=android;channel=mqqmina;mpVersion=0.33.3;qq_ver=8.3.9.2952;os_ver=Android 10;mpos_ver=1.16.0;platform=android;openid=BE8F85D640E85BB13C942C8DE01B0363");
        qqReadConfig.setBody("{ \"common\": { \"appid\": 1450024393,\"areaid\": 5,\"qq_ver\": \"8.3.9.2952\",\"os_ver\": \"Android 10\",\"mp_ver\": \"0.33.3\",\"mpos_ver\": \"1.16.0\",\"brand\": \"Xiaomi\",\"model\": \"MIX 2S\",\"screenWidth\": 393,\"screenHeight\": 738,\"windowWidth\": 393,\"windowHeight\": 684,\"openid\": \"BE8F85D640E85BB13C942C8DE01B0363\",\"guid\": 1398371419,\"session\": \"btges9uft294gal7uljxtxvb34ub8qjq\",\"scene\": 2014,\"source\": -1,\"hasRedDot\": \"false\",\"missions\": -1,\"caseID\": -1 },\"dataList\": [ { \"click1\": \"bookShelf_myshelf_myBook_C\",\"click2\": \"promoPopwin_close_C\",\"route\": \"pages/book-read/index\",\"refer\": \"pages/book-shelf/index\",\"options\": { \"bid\": \"319700\",\"cid\": \"7\",\"from\": \"shelf\" },\"dis\": 1607762013060,\"ext6\": 33,\"eventID\": \"bookRead_show_I\",\"type\": \"shown\",\"ccid\": 7,\"bid\": \"319700\",\"bookStatus\": 1,\"bookPay\": 0,\"chapterStatus\": 0,\"ext1\": { \"font\": 18,\"bg\": 1,\"pageMode\": 1 },\"from\": \"bookShelf_myshelf_myBook_C_0_319700\" } ] }");
        qqReadConfig.setBookUrl("https://mqqapi.reader.qq.com/mqq/addReadTimeWithBid?scene=2014&refer=-1&bid=27325720&readTime=5523&read_type=0&conttype=1&read_status=0&chapter_info=%5B%7B%229%22%3A%7B%22readTime%22%3A5523%2C%22pay_status%22%3A0%7D%7D%5D&sp=-1");
        var task = new QqReadTask(qqReadConfig);
        task.getDailyBeans();
    }
}