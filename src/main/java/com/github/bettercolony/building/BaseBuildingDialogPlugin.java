package com.github.bettercolony.building;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Misc;
import com.github.bettercolony.config.Expense;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.ResourceCostPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;

public abstract class BaseBuildingDialogPlugin<Option extends BaseOption> implements InteractionDialogPlugin {
    
    protected static Logger logger = Global.getLogger(BaseBuildingDialogPlugin.class);

    protected Map<String, MemoryAPI> memoryMap = new HashMap<>();

    public InteractionDialogAPI dialog;
    public TextPanelAPI textPanel;
	public OptionPanelAPI options;
    public VisualPanelAPI visual;
    public SectorEntityToken target;

    public CampaignFleetAPI playerFleet;
    public FactionAPI playerFaction;

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();
        target = dialog.getInteractionTarget();
        playerFleet = Global.getSector().getPlayerFleet();
        playerFaction = Global.getSector().getPlayerFaction();
        
        visual.setVisualFade(0.25f, 0.25f);

        dialog.setOptionOnEscape("Leave", Option.LEAVE);
		optionSelected(null, Option.INIT);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData == null || !(optionData instanceof BaseOption)) return;
		
        Option option = (Option) optionData;
        options.clearOptions();
        
        if (optionText != null) {
			textPanel.addParagraph(optionText, Global.getSettings().getColor("buttonText"));
        }
        
        if (option == Option.LEAVE) leaveDialog();
        else optionSelectedImpl(option, optionText);
    }

    public abstract void optionSelectedImpl(Option option, String optionText);

    public void leaveDialog() {
        Global.getSector().setPaused(false);
        dialog.dismiss();
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

    public boolean showCost(String title, List<Expense> cost) {
        ResourceCostPanelAPI panel = textPanel.addCostPanel(
            title, SalvageEntity.COST_HEIGHT,
            playerFaction.getBaseUIColor(), playerFaction.getDarkUIColor());

        panel.setWithBorder(false);
        panel.setAlignment(Alignment.LMID);
        panel.setNumberOnlyMode(true);

        boolean allPresent = true;
        CargoAPI cargo = playerFleet.getCargo();
        for (Expense expense: cost) {
            float inCargo = cargo.getCommodityQuantity(expense.id);
            if (inCargo > expense.amount) {
                panel.addCost(expense.id, String.format("%d (%d)", expense.amount, (int) inCargo));
            } else {
                allPresent = false;
                panel.addCost(expense.id, String.format("%d (%d)", expense.amount, (int) inCargo),
                    Misc.getNegativeHighlightColor());
            }
        }
        panel.update();

        return allPresent;
    }

    public void chargeCost(List<Expense> cost) {
        for (Expense expense: cost)
            playerFleet.getCargo().removeCommodity(expense.id, (float) expense.amount);
    }

    public static SectorEntityToken replaceEntity(StarSystemAPI system, SectorEntityToken target, String newEntityType, String factionId) {
        CustomCampaignEntityAPI newEntity = system.addCustomEntity(null, null, newEntityType, factionId);
        newEntity.setOrbit(target.getOrbit().makeCopy());
        system.removeEntity(target);
        return newEntity;
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
        // Not needed
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
        return memoryMap;
    }

}