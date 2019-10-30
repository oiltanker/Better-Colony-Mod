package com.github.bettercolony.config;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;

import org.json.JSONObject;

public final class Stations {
    public static class Station {

        public static List<Expense> getCost(String settingId) {
            List<Expense> res = new ArrayList<>();

            JSONObject costObj = Global.getSettings().getJSONObject(settingId).getJSONObject("cost");
            String[] costIds = JSONObject.getNames(costObj);
            for (String costId: costIds)
                res.add(new Expense(costId, costObj.getInt(costId)));

            return res;
        }

        public String type;
        public String locationType;
        public List<Expense> cost;

        public Station(String entityType, String locationEntityType, String settingId) {
            this.type = entityType;
            this.locationType = locationEntityType;
            this.cost = getCost(settingId);
        }
    }

    public static int MIN_STATIONS_OF_TYPE = Global.getSettings().getInt("minBuildableStationsOfType");
	public static int MAX_STATIONS_OF_TYPE = Global.getSettings().getInt("maxBuildableStationsOfType");
	public static float STATION_CLEARANCE = Global.getSettings().getFloat("buildableStationClearance");

    public static Station PROBE = new Station("makeshift_probe", "research_station_location", "buildableProbe");
    public static Station MINING = new Station("makeshift_mining_station", "mining_station_location", "buildableMiningStation");
    public static Station RESEARCH = new Station("makeshift_research_station", "research_station_location", "buildableResearchStation");
    public static Station COMMERCIAL = new Station("makeshift_commercial_station", "commercial_station_location", "buildableCommercialStation");
}