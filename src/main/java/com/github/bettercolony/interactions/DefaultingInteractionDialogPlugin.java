package com.github.bettercolony.interactions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;

import org.apache.log4j.Logger;

public class DefaultingInteractionDialogPlugin extends RuleBasedInteractionDialogPluginImpl {
    public static Logger logger = Global.getLogger(DefaultingInteractionDialogPlugin.class);

    public static Set<Class<? extends InteractionDialogListener>> listenerClasses = new HashSet<>();
    public static List<WeakReference<DefaultingInteractionDialogPlugin>> pluginInstances = new ArrayList<>();

    public static void register(Class<? extends InteractionDialogListener> listenerClass) {
        listenerClasses.add(listenerClass);
    }

    public static Set<InteractionDialogListener> instantiate() {
        try {
            Set<InteractionDialogListener> listeners = new HashSet<>();
            for (Class<? extends InteractionDialogListener> clazz : listenerClasses)
                listeners.add(clazz.newInstance());
            return listeners;
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(e.getStackTrace().toString());
            return null;
        }
    }
    public static void trigger(String trigger) {
        for (int i = 0; i < pluginInstances.size(); i++) {
            DefaultingInteractionDialogPlugin plugin = pluginInstances.get(i).get();
            if (plugin != null) plugin.onTrigger(trigger);
            else {
                pluginInstances.remove(i);
                i--;
            }
        }
    }


    public Set<InteractionDialogListener> listeners = instantiate();
    public String initialTrigger = "OpenInteractionDialog";


    public DefaultingInteractionDialogPlugin() {
        super();
        pluginInstances.add(new WeakReference<>(this));
    }
    public DefaultingInteractionDialogPlugin(String initialTrigger) {
        super(initialTrigger);
        this.initialTrigger = initialTrigger;
        pluginInstances.add(new WeakReference<>(this));
    }


    @Override
    public void init(InteractionDialogAPI dialog) {
        for (InteractionDialogListener listener : listeners) {
            listener.init(dialog, this);
            listener.optionSelected(null, initialTrigger);
        }
        super.init(dialog);
    }
    @Override
    public void optionSelected(String optionText, Object optionData) {
        for (InteractionDialogListener listener : listeners) listener.optionSelected(optionText, optionData);
        super.optionSelected(optionText, optionData);
    }
    public void onTrigger(String trigger) {
        for (InteractionDialogListener listener : listeners) listener.onTrigger(trigger);
    }
    @Override
	public void optionMousedOver(String optionText, Object optionData) {
        for (InteractionDialogListener listener : listeners) listener.optionMousedOver(optionText, optionData);
        super.optionMousedOver(optionText, optionData);
    }
    @Override
	public void advance(float amount) {
        for (InteractionDialogListener listener : listeners) listener.advance(amount);
        super.advance(amount);
    }
    @Override
	public void backFromEngagement(EngagementResultAPI battleResult) {
        for (InteractionDialogListener listener : listeners) listener.backFromEngagement(battleResult);
        super.backFromEngagement(battleResult);
    }
}