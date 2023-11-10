package com.example.backend.starter;

import com.example.backend.BackendApplication;
import com.example.backend.configuration.PluginAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
@Configuration(proxyBeanMethods = true)
@EnableConfigurationProperties(PluginAutoConfiguration.class)
@Import(BackendApplication.class)
public class PluginStarter {

}
