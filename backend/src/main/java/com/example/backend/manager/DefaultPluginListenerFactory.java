package com.example.backend.manager;

import com.example.backend.entity.PluginInfo;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
public class DefaultPluginListenerFactory implements PluginListener {

    @Getter
    private final List<PluginListener> listeners;

    public DefaultPluginListenerFactory(ApplicationContext applicationContext){
        this.listeners = new ArrayList<>();
        addExtendedPluginListener(applicationContext);
    }

    private void addExtendedPluginListener(ApplicationContext applicationContext){
        Map<String, PluginListener> beansOfTypeMap = applicationContext.getBeansOfType(PluginListener.class);
        if (!beansOfTypeMap.isEmpty()) {
            listeners.addAll(beansOfTypeMap.values());
        }
    }

    public synchronized void addSinglePluginListener(PluginListener pluginListener) {
        if(pluginListener != null){
            listeners.add(pluginListener);
        }
    }

    @Override
    public void startSuccess(PluginInfo pluginInfo) {
        for (PluginListener listener : listeners) {
            try {
                listener.startSuccess(pluginInfo);
            } catch (Exception e) {
                throw new RuntimeException(String.format("sending plugin starting success message failed. since %s", e.getMessage()), e);
            }
        }
    }

    @Override
    public void startFailure(PluginInfo pluginInfo, Throwable throwable) {
        for (PluginListener listener : listeners) {
            try {
                listener.startFailure(pluginInfo, throwable);
            } catch (Exception e) {
                throw new RuntimeException(String.format("sending plugin starting failure message failed. since %s", e.getMessage()), e);
            }
        }
    }

    @Override
    public void stopSuccess(PluginInfo pluginInfo) {
        for (PluginListener listener : listeners) {
            try {
                listener.stopSuccess(pluginInfo);
            } catch (Exception e) {
                throw new RuntimeException(String.format("sending plugin stopping success message failed. since %s", e.getMessage()), e);
            }
        }
    }

    @Override
    public void stopFailure(PluginInfo pluginInfo, Throwable throwable) {
        for (PluginListener listener : listeners) {
            try {
                listener.stopFailure(pluginInfo, throwable);
            } catch (Exception e) {
                throw new RuntimeException(String.format("sending plugin stopping failure message failed. since %s", e.getMessage()), e);
            }
        }
    }
}
