package com.colonizer.config;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;

import org.json.JSONObject;

public final class Stations {
    public static List<Expense> getCost(JSONObject costObj) {
        List<Expense> res = new ArrayList<>();

        String[] costIds = JSONObject.getNames(costObj);
        for (String costId: costIds)
            res.add(new Expense(costId, costObj.getInt(costId)));

        return res;
    }

    public static class Station {

        public String type;
        public String locationType;
        public List<Expense> cost;

        public Station(String entityType, String locationEntityType, String settingId) {
            this.type = entityType;
            this.locationType = locationEntityType;
            this.cost = Stations.getCost(Global.getSettings().getJSONObject(settingId).getJSONObject("cost"));
        }
    }
    
    public static int MIN_STATIONS_OF_TYPE = Global.getSettings().getInt("minBuildableStationsOfType");
	public static int MAX_STATIONS_OF_TYPE = Global.getSettings().getInt("maxBuildableStationsOfType");
	public static float STATION_CLEARANCE = Global.getSettings().getFloat("buildableStationClearance");

    public static List<Expense> COLONIZATION_COST = getCost(Global.getSettings().getJSONObject("colonizationCost"));
    
    public static Station MINING = new Station("makeshift_mining_station", "mining_station_location", "buildableMiningStation");
    public static Station COMMERCIAL = new Station("makeshift_commercial_station", "commercial_station_location", "buildableCommercialStation");
}