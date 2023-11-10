package com.example.backend.configuration;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PluginState {
    /**
     * 被禁用状态
     */
    DISABLED("DISABLED"),

    /**
     * 启动状态
     */
    STARTED("STARTED"),


    /**
     * 停止状态
     */
    STOPPED("STOPPED");

    private final String status;

}
