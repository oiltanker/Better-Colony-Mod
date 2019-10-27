package com.github.bettercolony;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public class BuildingCampaignPlugin extends BaseCampaignPlugin {

    public String getId() {
		return "MyCampaignPlugin_unique_id"; // make sure to change this for your mod
	}
	
	public boolean isTransient() {
		return false;
	}

	@SuppressWarnings("unchecked")
	public PluginPick pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
		// if the player is attempting to interact with an asteroid, 
        // return our custom dialog plugin.
		if (
            interactionTarget.getTags().contains(StationType.Mining) ||
            interactionTarget.getTags().contains(StationType.Research) ||
            interactionTarget.getTags().contains(StationType.Commercial)
        ) {
			return new PluginPick(
                new BuildingInteractionDialogPlugin(interactionTarget),
                PickPriority.MOD_GENERAL);
		}
		return null;
    }

}