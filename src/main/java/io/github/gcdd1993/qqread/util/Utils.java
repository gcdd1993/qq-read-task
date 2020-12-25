package io.github.gcdd1993.qqread.util;

import lombok.experimental.UtilityClass;

/**
 * @author gcdd1993
 * @date 2020/12/25
 * @since 1.0.0
 */
@UtilityClass
public class Utils {

    public static int randomInt(int min, int max) {
        return min + (int) (Math.random() * (max - min));
    }
}
