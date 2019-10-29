package com.github.bettercolony;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
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
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;

class BuildingInteractionDialogPlugin implements InteractionDialogPlugin {

    public static class OptionId {
		public static OptionId INIT = new OptionId();
		public static OptionId BUILD_STATION = new OptionId();
        public static OptionId CONFIRM_BUILD = new OptionId();
		public static OptionId LEAVE = new OptionId();
    }

    public static class Cost {
        String id;
        int amount;

        public Cost(String id, int amount) {
            this.id = id;
            this.amount = amount;
        }
    }

    public static class Costs {

        public static List<Cost> getCosts(String settingId) {
            List<Cost> res = new ArrayList<>();

            JSONObject costObj = Global.getSettings().getJSONObject(settingId).getJSONObject("cost");
            String[] costIds = JSONObject.getNames(costObj);
            for (String costId: costIds)
                res.add(new Cost(costId, costObj.getInt(costId)));

            return res;
        }

        public static List<Cost> MINING = getCosts("buildableMiningStation");
        public static List<Cost> RESEARCH_PROBE = getCosts("buildableResearchProbe");
        public static List<Cost> RESEARCH = getCosts("buildableResearchStation");
        public static List<Cost> COMMERCIAL = getCosts("buildableCommercialStation");
    }

    public InteractionDialogAPI dialog;
    public TextPanelAPI textPanel;
	public OptionPanelAPI options;
    public VisualPanelAPI visual;
    public SectorEntityToken target;

    public CampaignFleetAPI playerFleet;
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
        playerFleet = Global.getSector().getPlayerFleet();
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
            addText("Construction of a mining station would require following resources ...");

            options.addOption("Proceed with building the station", OptionId.CONFIRM_BUILD);
            options.addOption("Never mind", OptionId.INIT);

            if (!showCost("Mining station construction cost", Costs.MINING))
                options.setEnabled(OptionId.CONFIRM_BUILD, false);
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

    public boolean showCost(String title, List<Cost> cost) {
        ResourceCostPanelAPI panel = textPanel.addCostPanel(
            title, SalvageEntity.COST_HEIGHT,
            playerFaction.getBaseUIColor(), playerFaction.getDarkUIColor());

        panel.setWithBorder(false);
        panel.setAlignment(Alignment.LMID);
        panel.setNumberOnlyMode(true);

        boolean allPresent = true;
        CargoAPI cargo = playerFleet.getCargo();
        for (Cost c: cost) {
            float inCargo = cargo.getCommodityQuantity(c.id);
            if (inCargo > c.amount) {
                panel.addCost(c.id, String.format("%d (%d)", c.amount, (int) inCargo));
            } else {
                allPresent = false;
                panel.addCost(c.id, String.format("%d (%d)", c.amount, (int) inCargo),
                    Misc.getNegativeHighlightColor());
            }
        }
        panel.update();

        return allPresent;
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