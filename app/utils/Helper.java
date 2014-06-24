package utils;

import org.apache.commons.lang.StringUtils;

public class Helper {

    public static void notNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " should not be null");
        }
    }

    public static void notBlank(String value, String name) {
        notNull(value, name);
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(name + " should not be blank");
        }
    }
}
