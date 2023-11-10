package com.example.backend;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.backend.configuration.PluginAutoConfiguration;
import com.example.backend.manager.DefaultPluginManager;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractPluginApplication {

    private final AtomicBoolean hasInitialized = new AtomicBoolean(false);

    public synchronized void initialize(ApplicationContext applicationContext) {
        Objects.requireNonNull(applicationContext, "[Plugin] ApplicationContext can't be null");
        if (hasInitialized.get()) {
            throw new RuntimeException("[Plugin] Plugin has been initialized");
        }

        PluginAutoConfiguration configuration = parseConfiguration(applicationContext);

        if (Boolean.FALSE.equals(configuration.getEnable())) {
            log.warn("[Plugin] Cannot initialize plugins, since plugin.enable = false.");
            return;
        }

        try {
            log.info("[Plugin] environment: {}, plugin source path: {}", configuration.getRunMode(), String.join(",", configuration.getPluginPath()));
            initPluginManager(applicationContext).loadPlugins();

            hasInitialized.set(true);
            log.info("[Plugin] all plugins had been initialized.");
        } catch (Exception e) {
            log.error("[Plugin] something wrong happened when initialize plugin, since {}", e.getMessage(), e);
        }
    }

    protected PluginAutoConfiguration parseConfiguration(ApplicationContext applicationContext) {
        try {
            return applicationContext.getBean(PluginAutoConfiguration.class);
        } catch (Exception e) {
            throw new BeanCreationException("[Plugin] Cannot find a valid plugin configuration bean.", e);
        }
    }

    protected DefaultPluginManager initPluginManager(ApplicationContext applicationContext) {
        try {
            return applicationContext.getBean(DefaultPluginManager.class);
        } catch (Exception e) {
            throw new BeanCreationException("[Plugin] Cannot find a valid plugin manager bean.", e);
        }
    }
}

