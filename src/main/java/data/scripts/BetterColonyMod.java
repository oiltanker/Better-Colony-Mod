package data.scripts;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;

public class BetterColonyMod extends BaseModPlugin {
    public static float[] USE_RATE = {
        // 0.5f, 0.25f, 0.125f, 0.062f, 0.0405f, 0.0335f, 0.0295f, 0.0265f, 0.0225f, 0.0f
        0.5f, 0.25f, 0.125f, 0.125f, 0.125f, 0.125f, 0.125f, 0.125f, 0.125f, 0.125f
    };
    public static Logger logger = Global.getLogger(BetterColonyMod.class);

    public static void setUseRate() {
        logger.debug("-- BEFORE");
        logger.debug(CoreImmigrationPluginImpl.INCENTIVE_USE_RATE);
        logger.debug("-- REPLACE");
        logger.debug(USE_RATE);
        CoreImmigrationPluginImpl.INCENTIVE_USE_RATE = (float[]) USE_RATE.clone();
        logger.debug("-- RESULT");
        logger.debug(CoreImmigrationPluginImpl.INCENTIVE_USE_RATE);
    }

    @Override
    public void onApplicationLoad() {
        setUseRate();
    }
    @Override
    public void onEnabled(boolean wasEnabledBefore) {
        setUseRate();
    }
    @Override
    public void onGameLoad(boolean newGame) {
        setUseRate();
    }
}