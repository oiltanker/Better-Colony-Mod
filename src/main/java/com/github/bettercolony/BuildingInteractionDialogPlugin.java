package com.github.bettercolony;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.ResourceCostPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

class BuildingInteractionDialogPlugin implements InteractionDialogPlugin {

    public static class OptionId {
		public static OptionId INIT = new OptionId();
		public static OptionId BUILD_STATION = new OptionId();
        public static OptionId CONFIRM_BUILD = new OptionId();
		public static OptionId LEAVE = new OptionId();
	}

    public InteractionDialogAPI dialog;
    public TextPanelAPI textPanel;
	public OptionPanelAPI options;
    public VisualPanelAPI visual;
    public SectorEntityToken target;

    public FactionAPI playerFaction;
    
    BuildingInteractionDialogPlugin() {
        super();
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();
        target = (SectorEntityToken) dialog.getInteractionTarget();
        playerFaction = Global.getSector().getPlayerFaction();
        
        visual.setVisualFade(0.25f, 0.25f);

        dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		optionSelected(null, OptionId.INIT);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData == null) return;
		
        OptionId option = (OptionId) optionData;
        options.clearOptions();
        
        if (optionText != null) {
			textPanel.addParagraph(optionText, Global.getSettings().getColor("buttonText"));
        }
        
        if (option == OptionId.LEAVE) {
            Global.getSector().setPaused(false);
			dialog.dismiss();
        } else if (target.getTags().contains(StationType.Mining)) {
            miningOptionSelected(option, optionText, optionData);
        }
    }

    public void miningOptionSelected(OptionId option, String optionText, Object optionData) {
        if (option == OptionId.INIT) {
            clearText();
            if (target.getCustomDescriptionId() != null) {
                addText(Global.getSettings().getDescription(
                    target.getCustomDescriptionId(), Description.Type.CUSTOM).getText1());
            }

            visual.showImagePortion("illustrations", "free_orbit", 480, 300, 0, 0, 480, 300);
            addText("You decide to ...");

            options.addOption("Build a mining station", OptionId.BUILD_STATION);
            options.addOption("Leave", OptionId.LEAVE);
        } else if (option == OptionId.BUILD_STATION) {
            visual.showImagePortion("illustrations", "orbital", 480, 300, 0, 0, 480, 300);

            ResourceCostPanelAPI cost = textPanel.addCostPanel(
                "Mining station construction const", 1.0f,
                playerFaction.getBaseUIColor(), playerFaction.getDarkUIColor());
            cost.addCost("", 1);

            options.addOption("Proceed with building the station", OptionId.CONFIRM_BUILD);
            options.addOption("Never mind", OptionId.INIT);
        } else if (option == OptionId.CONFIRM_BUILD) {
            options.addOption("Continue", OptionId.LEAVE);
        }
    }

    public void addText(String text) {
		textPanel.addParagraph(text);
	}
	
	public void appendText(String text) {
		textPanel.appendToLastParagraph(" " + text);
    }
    
    public void clearText() {
        textPanel.clear();
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
        // Nt needed
    }

    @Override
    public void advance(float amount) {
        // Not a combat interaction
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
        // Not a combat interaction
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        // TODO: What is this?
        return null;
    }

}