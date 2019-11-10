package com.github.bettercolony.interactions.building;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.github.bettercolony.config.Expense;
import com.github.bettercolony.config.Stations;
import com.github.bettercolony.interactions.BaseInteractionDialogPlugin;
import com.github.bettercolony.interactions.InteractionDialogListener;
import com.github.bettercolony.interactions.Option;
import com.github.bettercolony.interactions.Triggers;

import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class ColonizationInteractionDialogListener extends InteractionDialogListener {

    public static Logger logger = Global.getLogger(ColonizationInteractionDialogListener.class);

    public static class ColonyOption {
        public static String INIT = Option.newOption();
        public static String DECONSTRUCT = Option.newOption();
        public static String DECONSTRUCT_CONFIRM = Option.newOption();
        public static String COLONIZE = Option.newOption();
        public static String COLONIZE_CONFIRM = Option.newOption();
    }

    public static String[] COLONIZE_TEXT = {
        /* Name */ " has a hazard rating of ", /* hazard_rating_percent */
        "%%. The upkeep cost of any industries and structures here will be increased by that percentage.",

        "A colony will allow for the exploitation of (if any) local resources and can eventually grow into a major population center.",

        "A colony established here would have an accessibility of ", /* accessability_percent */
        "%%, assuming a spaceport was constructed."
    };

    public boolean fittingTarget;
    public boolean isProbe;
    public String currentOption;

    public MarketAPI market;
    public int hazard_rating_percent;
    public int accessability_percent;

    @Override
    public void init(InteractionDialogAPI dialog) {
        if (target instanceof MarketAPI) target = ((MarketAPI) target).getPrimaryEntity();
        if (target == null) {
            fittingTarget = false;
            isProbe = false;
            return;
        }

        fittingTarget = target.getMarket() != null &&
            target.getMarket().hasCondition(Conditions.ABANDONED_STATION);

        if (fittingTarget && !isProbe) {
            market = createMarketFor(target, target.getMarket());
            market.setFactionId(Factions.PLAYER);
            market.reapplyConditions();
            market.reapplyIndustries();

            hazard_rating_percent = (int) (market.getHazardValue() * 100f);
            StatBonus access = market.getAccessibilityMod();
            accessability_percent = (int) (access.getFlatBonus() * access.getBonusMult() * 100f); // TODO: Does only take spaceport into consideration

            market.setFactionId(Factions.NEUTRAL);
            market.reapplyConditions();
            market.reapplyIndustries();

            Industry spaceport = market.getIndustry(Industries.SPACEPORT);
            spaceport.downgrade();
            spaceport.startBuilding();
        }
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (fittingTarget && optionData != null) {
            if (optionData.equals(ColonyOption.INIT)) {
                options.clearOptions();
                fireAll(Triggers.POPULATE_OPTIONS);
            } else if (optionData.equals(ColonyOption.COLONIZE)) {
                options.clearOptions();
                textPanel.addPara("%s" + COLONIZE_TEXT[0] + "%s" + COLONIZE_TEXT[1], Color.WHITE, Color.YELLOW,
                        target.getName(), String.valueOf(hazard_rating_percent));
                textPanel.addParagraph(COLONIZE_TEXT[2]);
                textPanel.addPara(COLONIZE_TEXT[3] + "%s" + COLONIZE_TEXT[4], Color.WHITE, Color.YELLOW,
                        String.valueOf(accessability_percent));
    
                options.addOption("Establish a colony", ColonyOption.COLONIZE_CONFIRM);
                options.addOption("Never mind", ColonyOption.INIT);
    
                if (!showCost("Resources: required (available)", Stations.COLONIZATION_COST, true) && !Global.getSettings().isDevMode())
                    options.setEnabled(ColonyOption.COLONIZE_CONFIRM, false);
            } else if (optionData.equals(ColonyOption.COLONIZE_CONFIRM)) {
                options.clearOptions();
                chargeCost(Stations.COLONIZATION_COST);
                visual.showImagePortion("illustrations", "orbital", 480, 300, 0, 0, 480, 300);
    
                target.setFaction(Factions.PLAYER);
                market.setFactionId(Factions.PLAYER);
                market.setPlayerOwned(true);

                market.addSubmarket(Submarkets.LOCAL_RESOURCES);
                market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
                ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
                for (Industry industry:  market.getIndustries()) {
                    industry.doPreSaveCleanup();
                    industry.doPostSaveRestore();
                }

                Global.getSector().getEconomy().removeMarket(target.getMarket());
                target.setMarket(market);
                market.setAdmin(Global.getSector().getPlayerPerson());
                Global.getSector().getEconomy().addMarket(market, true);
                
                textPanel.addParagraph("A colony has been successfully established.");
    
                options.addOption("Continue", "marketLeave");
                options.setShortcut("marketLeave", Keyboard.KEY_ESCAPE, false, false, false, false);
    
                int colonies = 0;
                for (MarketAPI market: Global.getSector().getEconomy().getMarketsCopy())
                    if (market.getFactionId().equals(Factions.PLAYER)) colonies++;
                if (colonies <= 1) {
                    Global.getSector().addTransientScript(new EveryFrameScript() {
                        CampaignUIAPI ui = Global.getSector().getCampaignUI();
                        boolean afterCloseDone = false;
            
                        @Override
                        public boolean isDone() {
                            return afterCloseDone;
                        }
            
                        @Override
                        public boolean runWhilePaused() {
                            if (!ui.isShowingDialog())
                                afterCloseDone = Global.getSector().getCampaignUI().showPlayerFactionConfigDialog();
                            return afterCloseDone;
                        }
            
                        @Override
                        public void advance(float amount) {
                            if(ui.isShowingDialog() || ui.isShowingMenu()) {
                                Global.getSector().setPaused(true);
                            }
                        }
                    });
                }
            } else if (optionData.equals(ColonyOption.DECONSTRUCT)) {
                options.clearOptions();
                textPanel.addParagraph("Are you sure, you want to brake " + target.getCustomEntitySpec().getNameInText()
                + " for salvage?");
                options.addOption("Proceed", ColonyOption.DECONSTRUCT_CONFIRM);
                options.addOption("Never mind", ColonyOption.INIT);
            } else if (optionData.equals(ColonyOption.DECONSTRUCT_CONFIRM)) {
                options.clearOptions();
                CargoAPI cargo = target.getCargo();
                cargo.addAll(target.getCargo());
                Random rand = new Random();
                rand.setSeed(target.hashCode());
                for (Expense expense: getCost()) {
                    cargo.addCommodity(expense.id,
                        (int) (expense.amount * (rand.nextFloat() * 0.4f + 0.5f)));
                }

                visual.showLoot("Salvaged", cargo, false, true, true, new CoreInteractionListener() {
                    @Override
                    public void coreUIDismissed() {
                        Global.getSector().getEconomy().removeMarket(target.getMarket());
                        BaseInteractionDialogPlugin.replaceEntity(target.getStarSystem(), target, getLocationType(), Factions.NEUTRAL);
                        dialog.dismiss();
                        Global.getSector().setPaused(false);
                    }
                });
            }
        }
    }

    @Override
    public void onTrigger(String trigger) {
        if (fittingTarget && trigger != null) {
            if (trigger.equals(Triggers.POPULATE_OPTIONS)) {
                options.addOption("Brake for salvage", ColonyOption.DECONSTRUCT);
                if (!isProbe) options.addOption("Establish a colony", ColonyOption.COLONIZE);
            }
        }
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
    }

    public List<Expense> getCost() {
        String type = target.getCustomEntityType();
        if (type == Stations.MINING.type)
            return Stations.MINING.cost;
        else if (type == Stations.COMMERCIAL.type)
            return Stations.COMMERCIAL.cost;
        else
            return null;
    }

    public String getLocationType() {
        String type = target.getCustomEntityType();
        if (type == Stations.MINING.type)
            return Stations.MINING.locationType;
        else if (type == Stations.COMMERCIAL.type)
            return Stations.COMMERCIAL.locationType;
        else
            return null;
    }

    @Override
    public void advance(float amount) {}

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {}

    public static MarketAPI createMarketFor(SectorEntityToken target, MarketAPI template) {
        MarketAPI market = Global.getFactory().createMarket("market_" + target.getId(), target.getName(), 3);

        market.setSize(3);
        market.setHidden(false);
        market.setPrimaryEntity(target);
        market.setFactionId(Factions.NEUTRAL);
        market.addCondition(Conditions.POPULATION_3);
        market.addIndustry(Industries.POPULATION);
        market.addIndustry(Industries.SPACEPORT);

        market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());

        market.setSurveyLevel(SurveyLevel.FULL);
        for (MarketConditionAPI mc: template.getConditions())
            if (!mc.getId().equals(Conditions.ABANDONED_STATION)) market.addCondition(mc.getId());
        for (MarketConditionAPI condition : market.getConditions())
            condition.setSurveyed(true);

        return market;
    }

}