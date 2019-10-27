package com.github.bettercolony;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.fs.starfarer.api.impl.campaign.procgen.themes.Themes;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import org.apache.log4j.Logger;

class BuildableStationGenerator extends BaseThemeGenerator {

	public static int MIN_STATIONS_OF_TYPE = Global.getSettings().getInt("minBuildableStationsOfType");
	public static int MAX_STATIONS_OF_TYPE = Global.getSettings().getInt("maxBuildableStationsOfType");
	public static float STATION_CLEARANCE = Global.getSettings().getFloat("buildableStationClearance");

	public static Logger logger = Global.getLogger(BuildableStationGenerator.class);

	List<StarSystemData> buildableStationSystems = new ArrayList<StarSystemData>();

	public BuildableStationGenerator() {
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
		for (Constellation constellation : context.constellations) {
			for (StarSystemAPI system : constellation.getSystems()) {
				StarSystemData data = new StarSystemData();
				data.system = system;
				data.gasGiants = new ArrayList<>();
				for (PlanetAPI planet : system.getPlanets())
					if (planet.isGasGiant())
						data.gasGiants.add(planet);
				for (SectorEntityToken stableLocation : system.getEntitiesWithTag("stable_location"))
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

	public static EntityLocation createLocationAtRandomGap(Random random, SectorEntityToken center, float minGap) {
		float ow = getOrbitalRadius(center);
		List<OrbitGap> gaps = findGaps(center, 1000f, 100f + ow + minGap, minGap);
		EntityLocation loc = createLocationAtRandomGap(random, center, gaps, LocationType.PLANET_ORBIT);
		return loc;
	}
	
	public static EntityLocation createLocationAtRandomGap(Random random, SectorEntityToken center, List<OrbitGap> gaps, LocationType type) {
		if (gaps.isEmpty()) return null;
		WeightedRandomPicker<OrbitGap> picker = new WeightedRandomPicker<OrbitGap>(random);
		picker.addAll(gaps);
		OrbitGap gap = picker.pick();
		return createLocationAtGap(center, gap, type);
	}
	
	public static EntityLocation createLocationAtGap(SectorEntityToken center, OrbitGap gap, LocationType type) {
		if (gap != null) {
			EntityLocation loc = new EntityLocation();
			loc.type = type;
			float orbitRadius = gap.start + (gap.end - gap.start) * (0.25f + 0.5f * StarSystemGenerator.random.nextFloat());
			float orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
			loc.orbit = Global.getFactory().createCircularOrbitWithSpin(center, 
					StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays, StarSystemGenerator.random.nextFloat() * 10f + 1f);
			return loc;
		}
		return null;
	}

	public void addBuildableLocations(
		String addMsg, String type,
		StarSystemAPI system, StarSystemData data, int min, int max,
		LinkedHashMap<LocationType, Float> weights
	) {
		int num = min + random.nextInt(max - min + 1);
		if (DEBUG) System.out.println("    Adding " + num + " " + addMsg);
		for (int i = 0; i < num; i++) {
			EntityLocation loc = null;
			if (weights != null) {
				WeightedRandomPicker<EntityLocation> locs = getLocations(random, data.system, data.alreadyUsed, STATION_CLEARANCE, weights);
				loc = locs.pick();
			} else {
				loc = createLocationAtRandomGap(random, system.getStar(), STATION_CLEARANCE);
			}
			
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

		addBuildableLocations(
			"mining locations", StationType.Mining,
			system, data, min, max, weights);
	}

	public void addResearchLocations(StarSystemAPI system, StarSystemData data, int min, int max) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.IN_SMALL_NEBULA, 5f);
		weights.put(LocationType.GAS_GIANT_ORBIT, 10f);
		weights.put(LocationType.NEAR_STAR, 5f);

		addBuildableLocations(
			"research locations", StationType.Research,
			system, data, min, max, weights);
	}

	public void addCommercialLocations(StarSystemAPI system, StarSystemData data, int min, int max) {
		addBuildableLocations(
			"commercial locations", StationType.Commercial,
			system, data, min, max, null);
	}

}