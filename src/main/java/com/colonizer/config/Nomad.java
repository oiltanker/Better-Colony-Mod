package com.colonizer.config;

import java.util.List;

import com.colonizer.common.Utils;
import com.fs.starfarer.api.Global;

import org.json.JSONObject;

public final class Nomad {
    public final static class Construction {
        public List<Expense> cost;
        public int time;
        public String entity;

        public Construction(String entity, JSONObject root, String settingId) {
            JSONObject settingsObj = root.getJSONObject(settingId);
            time = Utils.getTime(settingsObj);
            cost = Utils.getCost(settingsObj);
            this.entity = entity;
        }

        public static Construction STAGE_1 = new Construction("nomad_construction",
                Global.getSettings().getJSONObject("nomad").getJSONObject("construction"), "stage1");
        public static Construction STAGE_2 = new Construction("",
                Global.getSettings().getJSONObject("nomad").getJSONObject("construction"), "stage2");
    }
}