package com.colonizer.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.colonizer.config.Expense;

import org.json.JSONObject;

public class Utils {
    public static <T> String strJoin(String separator, Collection<T> collection) {
        String res = "";
        for (T elem: collection) res += separator + elem.toString();
        return res.substring(1);
    }

    public static List<Expense> getCost(JSONObject settingsObj) {
        JSONObject costObj = settingsObj.getJSONObject("cost");
        List<Expense> res = new ArrayList<>();

        String[] costIds = JSONObject.getNames(costObj);
        for (String costId: costIds)
            res.add(new Expense(costId, costObj.getInt(costId)));

        return res;
    }

    public static int getTime(JSONObject settingsObj) {
        return settingsObj.getInt("time");
    }
}