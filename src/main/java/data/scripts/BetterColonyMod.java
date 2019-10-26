package data.scripts;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;

public class BetterColonyMod extends BaseModPlugin {
    public static float[] USE_RATE = { 0.1464f, 0.1357f, 0.1250f, 0.1143f, 0.1036f, 0.0929f, 0.0821f, 0.0714f, 0.0607f, 0.0500f };
    public static int[] MAX_IND = { 1, 1, 1, 2, 3, 4, 5, 6, 7, 8 };
    public static Logger logger = Global.getLogger(BetterColonyMod.class);

    @Override
    public void onApplicationLoad() {
        CoreImmigrationPluginImpl.INCENTIVE_USE_RATE = (float[]) USE_RATE.clone();
        PopulationAndInfrastructure.MAX_IND = (int[]) MAX_IND.clone();
    }
    @Override
    public void onEnabled(boolean wasEnabledBefore) {
    }
    @Override
    public void onGameLoad(boolean newGame) {
    }
}