package com.github.bettercolony;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.fs.starfarer.api.impl.campaign.procgen.themes.Themes;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import org.lwjgl.util.vector.Vector2f;

class BuildableStaionGenerator extends BaseThemeGenerator {
	public static int MIN_STATIONS_OF_TYPE = Global.getSettings().getInt("minBuildableStationsOfType");
	public static int MAX_STATIONS_OF_TYPE = Global.getSettings().getInt("maxBuildableStationsOfType");
	public static float STATION_CLEARANCE = Global.getSettings().getFloat("buildableStationClearance");

    public static class StationType {
        public static String Commercial = "commercial_station_location";
        public static String Mining = "mining_station_location";
		public static String Research = "research_station_location";
	}
	
	List<StarSystemData> buildableStationSystems = new ArrayList<StarSystemData>();
    
    public BuildableStaionGenerator() {
		super();
	}

	public int getOrder() {
		return Integer.MAX_VALUE;
	}
	public String getThemeId() {
		return Themes.NO_THEME;
	}
	@Override
	public void generateForSector(ThemeGenContext context, float allowedUnusedFraction) {
		for (Constellation constellation: context.constellations) {
			for(StarSystemAPI system: constellation.getSystems()) {
				StarSystemData data = new StarSystemData();
				data.system = system;
				data.gasGiants = new ArrayList<>();
				for (PlanetAPI planet: system.getPlanets())
					if (planet.isGasGiant()) data.gasGiants.add(planet);
				for (SectorEntityToken stableLocation: system.getEntitiesWithTag("stable_location"))
					data.alreadyUsed.add(stableLocation);

				addBuildableLocations(system, data, MIN_STATIONS_OF_TYPE, MAX_STATIONS_OF_TYPE);
				buildableStationSystems.add(data);
			}
		}
	}

	public void addBuildableLocations(StarSystemAPI system, StarSystemData data, int min, int max) {
		addMiningLocations(system, data, min, max);
		addResearchLocations(system, data, min, max);
		addCommercialLocations(system, data, min, max);
	}

	public EntityLocation selectFreeLocation(StarSystemData data, WeightedRandomPicker<EntityLocation> locs) {
		EntityLocation loc = null;

		for(; loc == null && !locs.isEmpty(); ) {
			loc = locs.pick();
			locs.remove(loc);

			SectorEntityToken lFocus = (
				loc != null && loc.orbit != null
			) ? loc.orbit.getFocus(): null;

			if (lFocus != null) {
				Vector2f lBodyPos = loc.location;
				Vector2f lFocusPos = lFocus.getLocation();
				float lRadius = lFocus.getCircularOrbitRadius();

				for(SectorEntityToken entity: data.alreadyUsed) {
					Vector2f eBodyPos = entity.getLocation();
					Vector2f eFocusPos = (entity.getOrbit() != null && entity.getOrbit().getFocus() != null) ?
						entity.getOrbit().getFocus().getLocation() : null;
					float eRadius = entity.getCircularOrbitRadius();

					if (
						(
							lBodyPos != null && eBodyPos != null &&
							Misc.getDistance(lBodyPos, eBodyPos) < STATION_CLEARANCE
						) ||
						(
							lFocusPos != null && eFocusPos != null &&
							lFocusPos.equals(eFocusPos) && (Math.abs(eRadius - lRadius) < STATION_CLEARANCE)
						)
					) {
						loc = null;
					}
				}
			}
		}

		return loc;
	}

	public void addBuildableLocation(
		String addMsg, LinkedHashMap<LocationType, Float> weights, String type,
		StarSystemAPI system, StarSystemData data, int min, int max
	) {
		int num = min + random.nextInt(max - min + 1);
		if (DEBUG) System.out.println("    Adding " + num + " " + addMsg);
		for (int i = 0; i < num; i++) {
			
			WeightedRandomPicker<EntityLocation> locs = getLocations(random, data.system, data.alreadyUsed, 100f, weights);
			EntityLocation loc = selectFreeLocation(data, locs);
			
			if (loc != null) {
				AddedEntity added = addNonSalvageEntity(system, loc, type, Factions.NEUTRAL);
				if (added != null) {
					data.alreadyUsed.add(added.entity);
					data.generated.add(added);
					BaseThemeGenerator.convertOrbitPointingDown(added.entity);
				}
			}
		}
	}

    public void addMiningLocations(StarSystemAPI system, StarSystemData data, int min, int max) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.IN_ASTEROID_BELT, 10f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 10f);
		weights.put(LocationType.IN_RING, 10f);
		weights.put(LocationType.IN_SMALL_NEBULA, 10f);

		addBuildableLocation(
			"mining locations", weights, StationType.Mining,
			system, data, min, max);
	}

	public void addResearchLocations(StarSystemAPI system, StarSystemData data, int min, int max) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.IN_SMALL_NEBULA, 5f);
		weights.put(LocationType.GAS_GIANT_ORBIT, 10f);
		weights.put(LocationType.NEAR_STAR, 5f);

		addBuildableLocation(
			"research locations", weights, StationType.Research,
			system, data, min, max);
	}

	public void addCommercialLocations(StarSystemAPI system, StarSystemData data, int min, int max) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.JUMP_ORBIT, 10f);
		weights.put(LocationType.OUTER_SYSTEM, 10f);

		addBuildableLocation(
			"commercial locations", weights, StationType.Commercial,
			system, data, min, max);
	}
}