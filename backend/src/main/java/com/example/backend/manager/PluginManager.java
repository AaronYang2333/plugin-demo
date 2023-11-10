package com.example.backend.manager;

import com.example.backend.entity.PluginInfo;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.List;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
public interface PluginManager {

    List<PluginInfo> loadPlugins() throws Exception;

    PluginInfo install(Path pluginPath);

    void uninstall(String pluginId);

    PluginInfo start(String pluginId);

    PluginInfo stop(String pluginId);

    ApplicationContext getApplicationContext(String pluginId);

    List<Object> getBeansWithAnnotation(String pluginId, Class<? extends Annotation> annotationType);
}
