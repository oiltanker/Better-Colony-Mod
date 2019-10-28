package com.github.bettercolony;

import java.util.Collection;

public class Utils {
    public static <T> String strJoin(String separator, Collection<T> collection) {
        String res = "";
        for (T elem: collection) res += separator + elem.toString();
        return res.substring(1);
    }
}