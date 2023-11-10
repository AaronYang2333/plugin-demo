package com.example.backend.configuration;

import com.example.backend.manager.DefaultPluginManager;
import com.example.backend.manager.PluginManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
@Configuration
public class PluginConfiguration {
    @Bean
    public PluginManager createPluginManager(PluginAutoConfiguration configuration, ApplicationContext applicationContext) {
        return new DefaultPluginManager(configuration, applicationContext);
    }
}

