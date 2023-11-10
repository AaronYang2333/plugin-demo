package com.example.backend.entity;

import com.example.backend.configuration.PluginState;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
@Data
@Builder
public class PluginInfo {

    /**
     * 插件id
     */
    private String id;

    /**
     * 版本
     */
    private String version;

    /**
     * 描述
     */
    private String description;

    /**
     * 插件路径
     */
    private String path;

    /**
     * 插件启动状态
     */
    private PluginState pluginState;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PluginInfo other = (PluginInfo) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void setPluginState(PluginState started) {
        this.pluginState = started;
    }

}

