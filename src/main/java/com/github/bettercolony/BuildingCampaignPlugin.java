package com.github.bettercolony;

import java.util.Collection;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.github.bettercolony.config.Stations;
import com.github.bettercolony.building.*;

public class BuildingCampaignPlugin extends BaseCampaignPlugin {

    public String getId() {
		return "MyCampaignPlugin_unique_id"; // make sure to change this for your mod
	}
	
	public boolean isTransient() {
		return false;
	}

	public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
		Collection<String> tags = interactionTarget.getTags();
		if (tags.contains(Stations.MINING.locationType)) {
			return new PluginPick<InteractionDialogPlugin>(
                new MiningBuildingDialogPlugin(),
                PickPriority.MOD_GENERAL);
		} else if (tags.contains(Stations.RESEARCH.locationType)) {
			return new PluginPick<InteractionDialogPlugin>(
                null, // TODO: New plugin
                PickPriority.MOD_GENERAL);
		} else if (tags.contains(Stations.COMMERCIAL.locationType)) {
			return new PluginPick<InteractionDialogPlugin>(
                null, // TODO: New plugin
                PickPriority.MOD_GENERAL);
		}  else if (
			interactionTarget.getCustomEntityType() == Stations.PROBE.type ||
			interactionTarget.getCustomEntityType() == Stations.MINING.type ||
			interactionTarget.getCustomEntityType() == Stations.RESEARCH.type ||
			interactionTarget.getCustomEntityType() == Stations.COMMERCIAL.type
		) {
			return new PluginPick<InteractionDialogPlugin>(
                new ColonizationDialogPlugin(),
                PickPriority.MOD_GENERAL);
		}
		return null;
    }

}