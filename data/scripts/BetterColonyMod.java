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
    public Logger logger;

    public BetterColonyMod() {
        this.logger = Global.getLogger(this.getClass());
    }

    public void setUseRate() {
        this.logger.info("-- BEFORE");
        this.logger.info(CoreImmigrationPluginImpl.INCENTIVE_USE_RATE);
        this.logger.info("-- REPLACE");
        this.logger.info(USE_RATE);
        CoreImmigrationPluginImpl.INCENTIVE_USE_RATE = (float[]) USE_RATE.clone();
        this.logger.info("-- RESULT");
        this.logger.info(CoreImmigrationPluginImpl.INCENTIVE_USE_RATE);
    }

    @Override
    public void onApplicationLoad() {
        this.setUseRate();
    }
    @Override
    public void onEnabled(boolean wasEnabledBefore) {
        this.setUseRate();
    }
    @Override
    public void onGameLoad(boolean newGame) {
        this.setUseRate();
    }
}