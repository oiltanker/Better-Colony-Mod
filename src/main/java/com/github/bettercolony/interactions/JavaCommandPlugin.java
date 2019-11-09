package com.github.bettercolony.interactions;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

public class JavaCommandPlugin extends BaseCommandPlugin {

    public static Logger logger = Global.getLogger(JavaCommandPlugin.class);

    public InteractionDialogAPI dialog;
    public Map<String, MemoryAPI> memoryMap;
    
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
            List<Token> params, Map<String, MemoryAPI> memoryMap) {
        String command = params.get(0).getString(memoryMap);

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        if (
            command.equals(Triggers.POPULATE_OPTIONS) ||
            command.equals(Triggers.BEGIN_FLEET_ENCOUNTER) ||
            command.equals(Triggers.OPEN_COMM_LINK)
        ) {
            DefaultingInteractionDialogPlugin.trigger(command);
        }

        return true;
    }

}