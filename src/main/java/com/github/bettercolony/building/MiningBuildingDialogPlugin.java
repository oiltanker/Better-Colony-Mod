package com.github.bettercolony.building;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.github.bettercolony.config.Stations;
import com.fs.starfarer.api.loading.Description;

public class MiningBuildingDialogPlugin extends BaseBuildingDialogPlugin<MiningBuildingDialogPlugin.MiningOption> {
    public static class MiningOption extends BaseBuildingDialogPlugin.BaseOption {
		public static Option BUILD_STATION = new Option();
        public static Option CONFIRM_BUILD = new Option();
    }

    @Override
    public void optionSelectedImpl(Option option, String optionText) {
        if (option == MiningOption.INIT) {
            if (target.getCustomDescriptionId() != null) {
                addText(Global.getSettings().getDescription(
                    target.getCustomDescriptionId(), Description.Type.CUSTOM).getText1());
            }

            visual.showImagePortion("illustrations", "free_orbit", 480, 300, 0, 0, 480, 300);
            addText("You decide to ...");

            options.addOption("Build a mining station", MiningOption.BUILD_STATION);
            options.addOption("Leave", MiningOption.LEAVE);
        } else if (option == MiningOption.BUILD_STATION) {
            visual.showImagePortion("illustrations", "orbital_construction", 480, 300, 0, 0, 480, 300);
            addText("Construction of a mining station would require following resources ...");

            options.addOption("Proceed with building the station", MiningOption.CONFIRM_BUILD);
            options.addOption("Never mind", MiningOption.INIT);

            if (!showCost("Resources: required (available)", Stations.MINING.cost, false))
                options.setEnabled(MiningOption.CONFIRM_BUILD, false);
        } else if (option == MiningOption.CONFIRM_BUILD) {
            chargeCost(Stations.MINING.cost);
            replaceEntity(playerFleet.getStarSystem(), target, Stations.MINING.type, Factions.PLAYER);
            options.addOption("Continue", MiningOption.LEAVE);
        }
    }

    @Override
    public void onInit() {}

    @Override
    public void onLeave() {}
}