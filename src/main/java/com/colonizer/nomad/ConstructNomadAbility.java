package com.colonizer.nomad;

import com.colonizer.nomad.dialog.NomadBuildingDialogPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.*;

public class ConstructNomadAbility extends BaseDurationAbility {
    CampaignFleetAPI playerFleet;
    LocationAPI playerLocation;

    public ConstructNomadAbility() {
        playerFleet = Global.getSector().getPlayerFleet();
        playerLocation = playerFleet.getContainingLocation();
    }

    @Override
    protected void activateImpl() {
        // // Fleet
        // CampaignFleetAPI nomadFleet = FleetFactory.createGenericFleet(Factions.PLAYER, "Nomad fleet", 1f, 100);
        // nomadFleet.setContainingLocation(playerLocation);
        // nomadFleet.setLocation(playerFleet.getLocation().x, playerFleet.getLocation().y);
        // for (SectorEntityToken entity: playerLocation.getAllEntities()) {
        //     if (entity.isStar()) {
        //         playerLocation.spawnFleet(entity, playerFleet.getLocation().x, playerFleet.getLocation().y, nomadFleet);
        //         break;
        //     }
        // }
        // nomadFleet.setTransponderOn(true);

        // Market
        MarketAPI market = Global.getFactory().createMarket("market_nomad", "Nomad market", 3);

        market.setSize(3);
        market.setHidden(false);
        market.setFactionId(Factions.PLAYER);
        market.addCondition(Conditions.POPULATION_3);
        market.addIndustry(Industries.POPULATION);
        market.addIndustry(Industries.SPACEPORT);

        market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());

        market.setSurveyLevel(SurveyLevel.FULL);
        for (MarketConditionAPI condition : market.getConditions())
            condition.setSurveyed(true);
        market.setPlayerOwned(true);

        market.addSubmarket(Submarkets.LOCAL_RESOURCES);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
        for (Industry industry:  market.getIndustries()) {
            industry.doPreSaveCleanup();
            industry.doPostSaveRestore();
        }

        // Fleet
        CampaignFleetAPI nomadFleet = FleetFactory.createEmptyFleet(Factions.PLAYER, "Nomad fleet", market);
        nomadFleet.setContainingLocation(playerLocation);
        nomadFleet.setLocation(playerFleet.getLocation().x, playerFleet.getLocation().y);
        nomadFleet.setTransponderOn(true);
        for (SectorEntityToken entity: playerLocation.getAllEntities()) {
            if (entity.isStar()) {
                playerLocation.spawnFleet(entity, playerFleet.getLocation().x, playerFleet.getLocation().y, nomadFleet);
                break;
            }
        }
        nomadFleet.getFleetData().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "legion_Escort"));

        // Finalizing
        market.setPrimaryEntity(nomadFleet);
        market.setAdmin(Global.getSector().getPlayerPerson());
        nomadFleet.setMarket(market);
        Global.getSector().getEconomy().addMarket(market, true);

        Global.getSector().getCampaignUI().showInteractionDialog(new NomadBuildingDialogPlugin(), playerFleet);
    }

    @Override
    protected void applyEffect(float amount, float level) {
    }

    @Override
    protected void deactivateImpl() {
    }

    @Override
    protected void cleanupImpl() {
    }

}