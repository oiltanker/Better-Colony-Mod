package com.github.bettercolony.building;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.MarketInteractionMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.github.bettercolony.config.Stations;
import com.fs.starfarer.api.loading.Description;

public class ColonizationDialogPlugin extends BaseBuildingDialogPlugin<ColonizationDialogPlugin.ColonyOption> {

    public static class ColonyOption extends BaseBuildingDialogPlugin.BaseOption {
        public static Option DECONSTRUCT = new Option();
        public static Option CONFIRM_DECONSTRUCT = new Option();
        public static Option COLONIZE = new Option();
        public static Option COLONIZE_CONFIRM = new Option();
    }

    public static String[] COLONIZE_TEXT = {
        /*Name*/ " has a hazard rating of ", /*hazard_rating_percent*/
        "%%. The upkeep cost of any industries and structures here will be increased by that percentage.",

        "A colony will allow for the exploitation of (if any) local resources and can eventually grow into a major population center.",

        "A colony established here would have an accessibility of ", /*accessability_percent*/
        "%%, assuming a spaceport was constructed."
    };

    public MarketAPI market;
    public int hazard_rating_percent;
    public int accessability_percent;

    @Override
    public void onInit() {
        market = createMarketFor(target);

        market.setHasSpaceport(true);
        market.reapplyIndustries();

        hazard_rating_percent = (int) (market.getHazardValue() * 100f);
        accessability_percent = (int) (market.getAccessibilityMod().getFlatBonus() * market.getAccessibilityMod().getMult() * 100f);

        market.setHasSpaceport(false);
        market.reapplyIndustries();
    }

    public void dialogInit() {
        if (target.getCustomDescriptionId() != null) {
            addText(Global.getSettings().getDescription(
                target.getCustomDescriptionId(), Description.Type.CUSTOM).getText1());
        }

        visual.showImagePortion("illustrations", "orbital_construction", 480, 300, 0, 0, 480, 300);
        addText("You decide to ...");

        options.addOption("Brake for salvage", ColonyOption.DECONSTRUCT);
        if (target.getCustomEntityType() != Stations.PROBE.type)
            options.addOption("Establish a colony", ColonyOption.COLONIZE);
        options.addOption("Leave", ColonyOption.LEAVE);
    }

    @Override
    public void optionSelectedImpl(Option option, String optionText) {
        if (option == ColonyOption.INIT) {
            dialogInit();
        } else if (option == ColonyOption.COLONIZE) {
            textPanel.addPara(
                "%s" + COLONIZE_TEXT[0] + "%s" + COLONIZE_TEXT[1],
                Color.WHITE, Color.YELLOW, target.getName(), "100");
            addText(COLONIZE_TEXT[2]);
            textPanel.addPara(
                COLONIZE_TEXT[3] + "%s" + COLONIZE_TEXT[4],
                Color.WHITE, Color.YELLOW, String.valueOf(accessability_percent));

            options.addOption("Establish a colony", ColonyOption.COLONIZE_CONFIRM);
            options.addOption("Never mind", ColonyOption.INIT);

            if (!showCost("Resources: required (available)", Stations.COLONIZATION_COST, true))
                options.setEnabled(ColonyOption.COLONIZE_CONFIRM, false);
        } else if (option == ColonyOption.COLONIZE_CONFIRM) {
            visual.showImagePortion("illustrations", "orbital", 480, 300, 0, 0, 480, 300);

            addText("A colony has been successfully established.");
            target.setMarket(market);
            market.setAdmin(Global.getSector().getPlayerPerson());
            Global.getSector().getEconomy().addMarket(market, true);
            Global.getSector().getCampaignUI().showInteractionDialog(target);
            target.addTag("colonized");

            options.addOption("Continue", ColonyOption.LEAVE);
        } else if (option == ColonyOption.DECONSTRUCT) {
            addText("Are you sure, you want to brake " + target.getCustomEntitySpec().getNameInText() + " for salvage?");
            options.addOption("Proceed", ColonyOption.CONFIRM_DECONSTRUCT);
            options.addOption("Never mind", ColonyOption.INIT);
        }  else if (option == ColonyOption.CONFIRM_DECONSTRUCT) {
            String locationType = null;
            if (target.getCustomEntityType() == Stations.PROBE.type) locationType = Stations.PROBE.locationType;
            else if (target.getCustomEntityType() == Stations.MINING.type) locationType = Stations.MINING.locationType;
            else if (target.getCustomEntityType() == Stations.RESEARCH.type) locationType = Stations.RESEARCH.locationType;
            else if (target.getCustomEntityType() == Stations.COMMERCIAL.type) locationType = Stations.COMMERCIAL.locationType;

            replaceEntity(playerFleet.getStarSystem(), target, locationType, Factions.NEUTRAL);
            // TODO: Add loot screen
            leaveDialog();
        }
    }

    @Override
    public void onLeave() {}

    public static MarketAPI createMarketFor(SectorEntityToken target) {
        MarketAPI market = Global.getFactory().createMarket("market_" + target.getId(), target.getName(), 3);

        market.setSize(3);
        market.setHidden(false);
        market.setPrimaryEntity(target);
        market.setFactionId(Factions.PLAYER);
        market.setPlayerOwned(true);
        market.addCondition(Conditions.POPULATION_3);
        market.addIndustry(Industries.POPULATION);

        market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());
        market.addSubmarket(Submarkets.LOCAL_RESOURCES);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);

        market.setSurveyLevel(SurveyLevel.FULL);
        market.addCondition(Conditions.ORE_MODERATE);
        for (MarketConditionAPI condition : market.getConditions()) {
            condition.setSurveyed(true);
        }

        
        market.reapplyConditions();
        return market;
    }

}