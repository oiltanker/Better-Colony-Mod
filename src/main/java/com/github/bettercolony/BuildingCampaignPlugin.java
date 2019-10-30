package com.github.bettercolony;

import java.util.Collection;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.github.bettercolony.config.Stations;

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
		Collection<String> tags = interactionTarget.getTags();
		if (
            tags.contains(Stations.MINING.locationType) ||
            tags.contains(Stations.RESEARCH.locationType) ||
            tags.contains(Stations.COMMERCIAL.locationType)
        ) {
			return new PluginPick(
                new BuildingInteractionDialogPlugin(),
                PickPriority.MOD_GENERAL);
		}
		return null;
    }

}