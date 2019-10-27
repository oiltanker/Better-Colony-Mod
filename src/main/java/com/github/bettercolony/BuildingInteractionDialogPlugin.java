package com.github.bettercolony;

import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

class BuildingInteractionDialogPlugin implements InteractionDialogPlugin {

    SectorEntityToken target = null;
    
    BuildingInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        super();
        target = interactionTarget;
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        // TODO Auto-generated method stub

    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void advance(float amount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        // TODO Auto-generated method stub
        return null;
    }

}