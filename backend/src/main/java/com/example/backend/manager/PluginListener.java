package com.example.backend.manager;

import com.example.backend.entity.PluginInfo;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
public interface PluginListener {


    default void startSuccess(PluginInfo pluginInfo){}

    default void startFailure(PluginInfo pluginInfo, Throwable throwable){}

    default void stopSuccess(PluginInfo pluginInfo){}

    default void stopFailure(PluginInfo pluginInfo, Throwable throwable){}
}

