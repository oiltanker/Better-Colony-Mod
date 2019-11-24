package com.colonizer.config;

import java.util.ArrayList;
import java.util.List;

import com.colonizer.common.Utils;
import com.fs.starfarer.api.Global;

import org.json.JSONObject;

public final class Stations {

    public static class Station {

        public String type;
        public String locationType;
        public List<Expense> cost;
        public int time;

        public Station(String entityType, String locationEntityType, String settingId) {
            this.type = entityType;
            this.locationType = locationEntityType;
            JSONObject settingsObj = Global.getSettings().getJSONObject(settingId);
            this.cost = Utils.getCost(settingsObj);
            this.time = Utils.getTime(settingsObj);
        }
    }
    
    public static int MIN_STATIONS_OF_TYPE = Global.getSettings().getInt("minBuildableStationsOfType");
	public static int MAX_STATIONS_OF_TYPE = Global.getSettings().getInt("maxBuildableStationsOfType");
	public static float STATION_CLEARANCE = Global.getSettings().getFloat("buildableStationClearance");

    public static List<Expense> COLONIZATION_COST = Utils.getCost(Global.getSettings().getJSONObject("colonization"));
    
    public static Station MINING = new Station("makeshift_mining_station", "mining_station_location", "buildableMiningStation");
    public static Station COMMERCIAL = new Station("makeshift_commercial_station", "commercial_station_location", "buildableCommercialStation");
}