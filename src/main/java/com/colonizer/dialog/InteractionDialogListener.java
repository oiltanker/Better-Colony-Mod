package com.colonizer.dialog;

import java.util.List;

import com.colonizer.config.Expense;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.ResourceCostPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Misc;

public abstract class InteractionDialogListener {

    public DefaultingInteractionDialogPlugin basePlugin;

    public InteractionDialogAPI dialog;
    public TextPanelAPI textPanel;
	public OptionPanelAPI options;
    public VisualPanelAPI visual;
    public SectorEntityToken target;

    public PersonAPI playerPerson;
    public CampaignFleetAPI playerFleet;
    public FactionAPI playerFaction;

    public InteractionDialogListener() {}

    public void init(InteractionDialogAPI dialog, DefaultingInteractionDialogPlugin basePlugin) {
        this.basePlugin = basePlugin;

        this.dialog = dialog;
        textPanel = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();
        target = dialog.getInteractionTarget();

        playerPerson = Global.getSector().getPlayerPerson();
        playerFleet = Global.getSector().getPlayerFleet();
        playerFaction = Global.getSector().getPlayerFaction();

        init(dialog);
    }

    public abstract void init(InteractionDialogAPI dialog);
    public abstract void optionSelected(String optionText, Object optionData);
    public abstract void onTrigger(String trigger);
    public abstract void optionMousedOver(String optionText, Object optionData);
	public abstract void advance(float amount);
    public abstract void backFromEngagement(EngagementResultAPI battleResult);
    
    public boolean fireAll(String trigger) {
		return FireAll.fire(null, dialog, basePlugin.getMemoryMap(), trigger);
	}
	
	public boolean fireBest(String trigger) {
		return FireBest.fire(null, dialog, basePlugin.getMemoryMap(), trigger);
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

    public boolean showCost(String title, List<Expense> cost, boolean border) {
        ResourceCostPanelAPI panel = textPanel.addCostPanel(
            title, SalvageEntity.COST_HEIGHT,
            playerFaction.getBaseUIColor(), playerFaction.getDarkUIColor());

        panel.setWithBorder(border);
        panel.setAlignment(Alignment.MID);
        panel.setNumberOnlyMode(true);

        boolean allPresent = true;
        CargoAPI cargo = playerFleet.getCargo();
        for (Expense expense: cost) {
            float inCargo = cargo.getCommodityQuantity(expense.id);
            if (inCargo >= expense.amount) {
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
}