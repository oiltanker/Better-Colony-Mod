package com.github.bettercolony.building;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.github.bettercolony.config.Stations;
import com.fs.starfarer.api.loading.Description;

public class ColonizationDialogPlugin extends BaseBuildingDialogPlugin<ColonizationDialogPlugin.ColonyOption> {
    public static class ColonyOption extends BaseOption {
        public static ColonyOption DECONSTRUCT = new ColonyOption();
        public static ColonyOption CONFIRM_DECONSTRUCT = new ColonyOption();
		public static ColonyOption COLONIZE = new ColonyOption();
    }

    @Override
    public void optionSelectedImpl(ColonyOption option, String optionText) {
        if (option == ColonyOption.INIT) {
            if (target.getCustomDescriptionId() != null) {
                addText(Global.getSettings().getDescription(
                    target.getCustomDescriptionId(), Description.Type.CUSTOM).getText1());
            }

            visual.showImagePortion("illustrations", "orbital", 480, 300, 0, 0, 480, 300);
            addText("You decide to ...");

            options.addOption("Brake for salvage", ColonyOption.DECONSTRUCT);
            if (target.getCustomEntityType() != Stations.PROBE.type)
                options.addOption("Establish a colony", ColonyOption.COLONIZE);
            options.addOption("Leave", ColonyOption.LEAVE);
        } else if (option == ColonyOption.COLONIZE) {
            // TODO: Add colonization screen
            leaveDialog();
        } else if (option == ColonyOption.DECONSTRUCT) {
            addText("Are you sure, you want to brake " + target.getCustomEntitySpec().getNameInText() + " for salvage?");
            options.addOption("Proceed", ColonyOption.CONFIRM_DECONSTRUCT);
            options.addOption("Never mind", ColonyOption.INIT);
        }  else if (option == ColonyOption.DECONSTRUCT) {
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
}