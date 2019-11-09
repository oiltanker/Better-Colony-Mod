package com.github.bettercolony.interactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Misc;
import com.github.bettercolony.config.Expense;
import com.github.bettercolony.interactions.Option;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
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
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;

public abstract class BaseInteractionDialogPlugin<TOption extends BaseInteractionDialogPlugin.BaseOption> implements InteractionDialogPlugin {

    public static class BaseOption {
        public static String INIT = Option.newOption();
        public static String LEAVE = Option.newOption();
    }
    
    protected static Logger logger = Global.getLogger(BaseInteractionDialogPlugin.class);

    protected MemoryAPI memory;
    protected Map<String, MemoryAPI> memoryMap;

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

        updateMemory();

        dialog.setOptionOnEscape("Leave", TOption.LEAVE);
        onInit();

		optionSelected(null, TOption.INIT);
    }

    public void updateMemory() {
		if (memoryMap == null) {
			memoryMap = new HashMap<String, MemoryAPI>();
		} else {
			memoryMap.clear();
		}
		memory = dialog.getInteractionTarget().getMemory();

		memoryMap.put(MemKeys.LOCAL, memory);
		if (dialog.getInteractionTarget().getFaction() != null) {
			memoryMap.put(MemKeys.FACTION, dialog.getInteractionTarget().getFaction().getMemory());
		} else {
			memoryMap.put(MemKeys.FACTION, Global.getFactory().createMemory());
		}
		memoryMap.put(MemKeys.GLOBAL, Global.getSector().getMemory());
		memoryMap.put(MemKeys.PLAYER, Global.getSector().getCharacterData().getMemory());

		if (dialog.getInteractionTarget().getMarket() != null) {
			memoryMap.put(MemKeys.MARKET, dialog.getInteractionTarget().getMarket().getMemory());
		}

		if (memory.contains(MemFlags.MEMORY_KEY_SOURCE_MARKET)) {
			String marketId = memory.getString(MemFlags.MEMORY_KEY_SOURCE_MARKET);
			MarketAPI market = Global.getSector().getEconomy().getMarket(marketId);
			if (market != null) {
				memoryMap.put(MemKeys.SOURCE_MARKET, market.getMemory());
			}
		}

		updatePersonMemory();
    }
    
    private void updatePersonMemory() {
		PersonAPI person = dialog.getInteractionTarget().getActivePerson();
		if (person != null) {
			memory = person.getMemory();
			memoryMap.put(MemKeys.LOCAL, memory);
			memoryMap.put(MemKeys.PERSON_FACTION, person.getFaction().getMemory());
			memoryMap.put(MemKeys.ENTITY, dialog.getInteractionTarget().getMemory());
		} else {
			memory = dialog.getInteractionTarget().getMemory();
			memoryMap.put(MemKeys.LOCAL, memory);
			memoryMap.remove(MemKeys.ENTITY);
			memoryMap.remove(MemKeys.PERSON_FACTION);
			
		}
    }
    
    public boolean fireAll(String trigger) {
		return FireAll.fire(null, dialog, memoryMap, trigger);
	}
	
	public boolean fireBest(String trigger) {
		return FireBest.fire(null, dialog, memoryMap, trigger);
	}

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData == null) return;
		
        String option = (String) optionData;
        options.clearOptions();
        
        if (optionText != null) {
			textPanel.addParagraph(optionText, Global.getSettings().getColor("buttonText"));
        }
        
        if (option == TOption.LEAVE) leaveDialog();
        else optionSelectedImpl(option, optionText);
    }

    public void optionSelectedImpl(String option, String optionText) {}
    public void onInit() {}
    public void onLeave() {}
    public boolean onAfterClose(CampaignUIAPI uiString) {
        return false;
    }

    public void leaveDialog() {
        dialog.dismiss();
        onLeave();
        Global.getSector().setPaused(false);
        // Pause and run after closing the dialog
        class AfterClose implements EveryFrameScript {
            CampaignUIAPI ui = Global.getSector().getCampaignUI();
            boolean afterCloseDone = false;

            @Override
            public boolean isDone() {
                return afterCloseDone;
            }

            @Override
            public boolean runWhilePaused() {
                afterCloseDone = onAfterClose(ui);
                return afterCloseDone;
            }

            @Override
            public void advance(float amount) {
                if(ui.isShowingDialog() || ui.isShowingMenu()) {
                    Global.getSector().setPaused(true);
                }
            }
        }
        Global.getSector().addTransientScript(new AfterClose());
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