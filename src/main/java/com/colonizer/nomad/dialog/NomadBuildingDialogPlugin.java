package com.colonizer.nomad.dialog;

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
import com.colonizer.config.Nomad;
import com.colonizer.config.Stations;
import com.colonizer.dialog.BaseInteractionDialogPlugin;
import com.colonizer.dialog.DefaultingInteractionDialogPlugin;
import com.colonizer.dialog.Option;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;

public class NomadBuildingDialogPlugin extends BaseInteractionDialogPlugin<NomadBuildingDialogPlugin.NomadOption> {
    public static class NomadOption extends BaseInteractionDialogPlugin.BaseOption {
		public static String BUILD_NOMAD = Option.newOption();
        public static String CONFIRM_BUILD = Option.newOption();
    }

    @Override
    public void optionSelectedImpl(String option, String optionText) {
        if (option == NomadOption.INIT) {
            if (target.getCustomDescriptionId() != null) {
                addText(Global.getSettings().getDescription(
                    target.getCustomDescriptionId(), Description.Type.CUSTOM).getText1());
            }

            visual.showImagePortion("illustrations", "free_orbit", 480, 300, 0, 0, 480, 300);
            addText("You decide to ...");

            options.addOption("Assemble nomad construction site", NomadOption.BUILD_NOMAD);
            options.addOption("Leave", NomadOption.LEAVE);
            options.setShortcut(NomadOption.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, false);
        } else if (option == NomadOption.BUILD_NOMAD) {
            visual.showImagePortion("illustrations", "orbital_construction", 480, 300, 0, 0, 480, 300);
            addText("Construction of a mining station would require following resources ...");

            options.addOption("Proceed with building the station", NomadOption.CONFIRM_BUILD);
            options.addOption("Never mind", NomadOption.INIT);

            if (!showCost("Resources: required (available)", Nomad.Construction.STAGE_1.cost, false) && !Global.getSettings().isDevMode())
                options.setEnabled(NomadOption.CONFIRM_BUILD, false);
        } else if (option == NomadOption.CONFIRM_BUILD) {
            chargeCost(Nomad.Construction.STAGE_1.cost);
            
            SectorEntityToken station = replaceEntity(playerFleet.getStarSystem(), target, Nomad.Construction.STAGE_1.entity, Factions.PLAYER);
            LocationAPI location = station.getContainingLocation();
            station.setName(location.getNameWithTypeIfNebula() + " " + station.getCustomEntitySpec().getNameInText());

            MarketAPI market = Global.getFactory().createMarket("market_" + station.getId(), station.getName(), 0);

            market.setFactionId(Factions.NEUTRAL);
            market.setHidden(false);
            market.setSize(0);
            market.addCondition(Conditions.ABANDONED_STATION);
            market.addCondition(Conditions.ORE_MODERATE);
            market.addCondition(Conditions.RARE_ORE_SPARSE);

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