package com.example.backend.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
@Data
@ConfigurationProperties(prefix = "plugin")
public class PluginAutoConfiguration {

    @Value("${enable:false}")
    private Boolean enable;

    @Value("${runMode:dev}")
    private String runMode;

    private List<String> pluginPath;

    @Value("${backupPath:backupPlugin}")
    private String backupPath;

    public RuntimeMode environment() {
        return RuntimeMode.byName(runMode);
    }
}
