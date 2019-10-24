package data.scripts;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;

public class BetterColonyMod extends BaseModPlugin {
    public static float[] USE_RATE = {
        // 0.5f, 0.25f, 0.125f, 0.062f, 0.0405f, 0.0335f, 0.0295f, 0.0265f, 0.0225f, 0.0f
        0.5f, 0.25f, 0.125f, 0.125f, 0.125f, 0.125f, 0.125f, 0.125f, 0.125f, 0.125f
    };
    public static int[] MAX_IND = {
        1, 1, 1, 2, 3, 4, 5, 6, 7, 8
    };
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