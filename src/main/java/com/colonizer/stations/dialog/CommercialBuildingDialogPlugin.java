package com.colonizer.stations.dialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.colonizer.config.Stations;
import com.colonizer.dialog.BaseInteractionDialogPlugin;
import com.colonizer.dialog.DefaultingInteractionDialogPlugin;
import com.colonizer.dialog.Option;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;

public class CommercialBuildingDialogPlugin extends BaseInteractionDialogPlugin<MiningBuildingDialogPlugin.MiningOption> {
    public static class CommercialOption extends BaseInteractionDialogPlugin.BaseOption {
		public static String BUILD_STATION = Option.newOption();
        public static String CONFIRM_BUILD = Option.newOption();
    }

    @Override
    public void optionSelectedImpl(String option, String optionText) {
        if (option == CommercialOption.INIT) {
            if (target.getCustomDescriptionId() != null) {
                addText(Global.getSettings().getDescription(
                    target.getCustomDescriptionId(), Description.Type.CUSTOM).getText1());
            }

            visual.showImagePortion("illustrations", "free_orbit", 480, 300, 0, 0, 480, 300);
            addText("You decide to ...");

            options.addOption("Build a commercial station", CommercialOption.BUILD_STATION);
            options.addOption("Leave", CommercialOption.LEAVE);
            options.setShortcut(CommercialOption.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, false);
        } else if (option == CommercialOption.BUILD_STATION) {
            visual.showImagePortion("illustrations", "orbital_construction", 480, 300, 0, 0, 480, 300);
            addText("Construction of a commercial station would require following resources ...");

            options.addOption("Proceed with building the station", CommercialOption.CONFIRM_BUILD);
            options.addOption("Never mind", CommercialOption.INIT);

            if (!showCost("Resources: required (available)", Stations.COMMERCIAL.cost, false) && !Global.getSettings().isDevMode())
                options.setEnabled(CommercialOption.CONFIRM_BUILD, false);
        } else if (option == CommercialOption.CONFIRM_BUILD) {
            chargeCost(Stations.COMMERCIAL.cost);
            
            SectorEntityToken station = replaceEntity(playerFleet.getStarSystem(), target, Stations.COMMERCIAL.type, Factions.NEUTRAL);
            LocationAPI location = station.getContainingLocation();
            station.setName(location.getNameWithTypeIfNebula() + " " + station.getCustomEntitySpec().getNameInText());

            MarketAPI market = Global.getFactory().createMarket("market_" + station.getId(), station.getName(), 0);

            market.setFactionId(Factions.NEUTRAL);
            market.setHidden(false);
            market.setSize(0);
            market.addCondition(Conditions.ABANDONED_STATION);
            market.addCondition(Conditions.URBANIZED_POLITY);

            market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
            ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
            for (Industry industry:  market.getIndustries()) {
                industry.doPreSaveCleanup();
                industry.doPostSaveRestore();
            }

            market.setSurveyLevel(SurveyLevel.FULL);
            for (MarketConditionAPI condition : market.getConditions()) condition.setSurveyed(true);

            market.setPrimaryEntity(station);
            station.setMarket(market);
            Global.getSector().getEconomy().addMarket(market, false);

            dialog.setInteractionTarget(station);
            InteractionDialogPlugin plugin = new DefaultingInteractionDialogPlugin();
            dialog.setPlugin(plugin);
            plugin.init(dialog);
        }
    }
}