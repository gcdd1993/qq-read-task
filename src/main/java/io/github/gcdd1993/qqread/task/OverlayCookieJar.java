package io.github.gcdd1993.qqread.task;

import lombok.NoArgsConstructor;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 将response的cookie加入到当前Cookie池
 *
 * @author gcdd1993
 * @date 2020/12/15
 * @since 1.0.0
 */
@NoArgsConstructor
public class OverlayCookieJar
        implements CookieJar {
    private final List<Cookie> cookies = new ArrayList<>();

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public OverlayCookieJar(String domain, String cookieStr) {
        Arrays.stream(cookieStr.split(";")).forEach(c -> {
            var split = c.split("=");
            var cookie = new Cookie.Builder()
                    .domain(domain)
                    .path("/")
                    .name(split[0].trim())
                    .value(split[1].trim())
                    .httpOnly()
                    .secure()
                    .build();
            addCookie(cookie);
        });
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
        this.cookies.addAll(list);
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        return this.cookies;
    }
}
