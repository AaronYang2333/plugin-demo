package com.example.backend.manager;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import com.example.backend.configuration.PluginAutoConfiguration;
import com.example.backend.configuration.PluginConstants;
import com.example.backend.configuration.PluginState;
import com.example.backend.configuration.RuntimeMode;
import com.example.backend.entity.PluginInfo;
import com.example.backend.exception.PluginException;
import com.example.backend.util.DeployUtils;
//import org.apache.maven.model.Model;
//import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
//import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.context.ApplicationContext;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.PathUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileSystemUtils;

@Slf4j
public class DefaultPluginManager implements PluginManager {

    private final PluginAutoConfiguration pluginAutoConfiguration;
    private final DefaultPluginListenerFactory pluginListenerFactory;
    private final PluginClassRegister pluginClassRegister;
    private final Map<String, ApplicationContext> pluginBeans = new ConcurrentHashMap<>();
    private final Map<String, PluginInfo> pluginInfoMap = new ConcurrentHashMap<>();
    private final AtomicBoolean loaded = new AtomicBoolean(false);


    public DefaultPluginManager(PluginAutoConfiguration pluginAutoConfiguration,
                                ApplicationContext applicationContext) {
        this.pluginAutoConfiguration = pluginAutoConfiguration;
        this.pluginClassRegister = new PluginClassRegister(applicationContext, pluginAutoConfiguration, pluginBeans);
        this.pluginListenerFactory = new DefaultPluginListenerFactory(applicationContext);
    }

    @Override
    public List<PluginInfo> loadPlugins() throws Exception {
        if (loaded.get()) {
            throw new PluginException("[Plugin] plugins had been loaded.");
        }

        List<PluginInfo> pluginInfoList = loadPluginsFromPath(pluginAutoConfiguration.getPluginPath());
        if (CollUtil.isEmpty(pluginInfoList)) {
            log.warn("路径下未发现任何插件");
            return pluginInfoList;
        }

        //注册插件
        for (PluginInfo pluginInfo : pluginInfoList) {
            start(pluginInfo);
        }
        loaded.set(true);
        return pluginInfoList;
    }

    private List<PluginInfo> loadPluginsFromPath(List<String> pluginPath) {
        return pluginPath.stream().flatMap(path -> {
            try {
                return buildPluginInfo(Paths.get(path)).stream();
            } catch (IOException e) {
                throw new PluginException(String.format("[Plugin] load plugin from path %s failed.", path), e);
            }
        }).collect(Collectors.toList());
    }

    private Set<PluginInfo> buildPluginInfo(Path path) throws IOException {
        Set<PluginInfo> pluginInfoList = new HashSet<>();
        //开发环境
        if (RuntimeMode.DEV == pluginAutoConfiguration.environment()) {
            List<File> pomFiles = FileUtil.loopFiles(path.toString(), file -> PluginConstants.POM.equals(file.getName()));
//            for (File file : pomFiles) {
//                MavenXpp3Reader reader = new MavenXpp3Reader();
//                Model model = reader.read(new FileInputStream(file));
//                PluginInfo pluginInfo = PluginInfo.builder().id(model.getArtifactId())
//                        .version(model.getVersion() == null ? model.getParent().getVersion() : model.getVersion())
//                        .description(model.getDescription()).build();
//                //开发环境重新定义插件路径，需要指定到classes目录
//                pluginInfo.setPath(CharSequenceUtil.subBefore(path.toString(), pluginInfo.getId(), false)
//                        + File.separator + pluginInfo.getId()
//                        + File.separator + PluginConstants.TARGET
//                        + File.separator + PluginConstants.CLASSES);
//                pluginInfoList.add(pluginInfo);
//            }
        }

        //生产环境从jar包中读取
        if (RuntimeMode.PROD == pluginAutoConfiguration.environment()) {
            //获取jar包列表
            List<File> jarFiles = FileUtil.loopFiles(path.toString(), file -> file.getName().endsWith(PluginConstants.JAR_SUFFIX));
            for (File jarFile : jarFiles) {
                //读取配置
                try (InputStream jarFileInputStream = DeployUtils.readManifestJarFile(jarFile)) {
                    Manifest manifest = new Manifest(jarFileInputStream);
                    Attributes attr = manifest.getMainAttributes();
                    PluginInfo pluginInfo = PluginInfo.builder().id(attr.getValue(PluginConstants.PLUGINID))
                            .version(attr.getValue(PluginConstants.PLUGINVERSION))
                            .description(attr.getValue(PluginConstants.PLUGINDESCRIPTION))
                            .path(jarFile.getPath()).build();
                    pluginInfoList.add(pluginInfo);
                } catch (Exception e) {
                    log.warn("插件{}配置读取异常", jarFile.getName());
                }
            }
        }
        return pluginInfoList;
    }

    @Override
    public PluginInfo install(Path pluginPath) {
        if (RuntimeMode.PROD != pluginAutoConfiguration.environment()) {
            throw new PluginException("插件安装只适用于生产环境");
        }
        try {
            Set<PluginInfo> pluginInfos = buildPluginInfo(pluginPath);
            if (CollUtil.isEmpty(pluginInfos)) {
                throw new PluginException("插件不存在");
            }
            PluginInfo pluginInfo = (PluginInfo) pluginInfos.toArray()[0];
            if (pluginInfoMap.get(pluginInfo.getId()) != null) {
                log.info("已存在同类插件{}，将覆盖安装", pluginInfo.getId());
            }
            uninstall(pluginInfo.getId());
            start(pluginInfo);
            return pluginInfo;
        } catch (Exception e) {
            throw new PluginException("插件安装失败", e);
        }
    }

    private void start(PluginInfo pluginInfo) {
        try {
            pluginClassRegister.register(pluginInfo);
            pluginInfo.setPluginState(PluginState.STARTED);
            pluginInfoMap.put(pluginInfo.getId(), pluginInfo);
            log.info("插件{}启动成功", pluginInfo.getId());
            pluginListenerFactory.startSuccess(pluginInfo);
        } catch (Exception e) {
            log.error("插件{}注册异常", pluginInfo.getId(), e);
            pluginListenerFactory.startFailure(pluginInfo, e);
        }
    }

    @Override
    public void uninstall(String pluginId) {
        if (RuntimeMode.PROD != pluginAutoConfiguration.environment()) {
            throw new PluginException("插件卸载只适用于生产环境");
        }
        PluginInfo pluginInfo = pluginInfoMap.get(pluginId);
        if (pluginInfo == null) {
            return;
        }
        stop(pluginInfo);
        backupPlugin(pluginInfo);
        clear(pluginInfo);
    }

    @Override
    public PluginInfo start(String pluginId) {
        PluginInfo pluginInfo = pluginInfoMap.get(pluginId);
        start(pluginInfo);
        return pluginInfo;
    }

    @Override
    public PluginInfo stop(String pluginId) {
        PluginInfo pluginInfo = pluginInfoMap.get(pluginId);
        stop(pluginInfo);
        return pluginInfo;
    }

    private void clear(PluginInfo pluginInfo) {
        PathUtil.del(Paths.get(pluginInfo.getPath()));
        pluginInfoMap.remove(pluginInfo.getId());
    }

    private void stop(PluginInfo pluginInfo) {
        try {
            pluginClassRegister.unRegister(pluginInfo);
            pluginInfo.setPluginState(PluginState.STOPPED);
            pluginListenerFactory.stopSuccess(pluginInfo);
            log.info("插件{}停止成功", pluginInfo.getId());
        } catch (Exception e) {
            log.error("插件{}停止异常", pluginInfo.getId(), e);
        }
    }

    private void backupPlugin(PluginInfo pluginInfo) {
        String backupPath = pluginAutoConfiguration.getBackupPath();
        if (CharSequenceUtil.isBlank(backupPath)) {
            return;
        }
        String newName = pluginInfo.getId() + DateUtil.now() + PluginConstants.JAR_SUFFIX;
        String newPath = backupPath + File.separator + newName;
        FileUtil.copyFile(pluginInfo.getPath(), newPath);
    }

    @Override
    public ApplicationContext getApplicationContext(String pluginId) {
        return pluginBeans.get(pluginId);
    }

    @Override
    public List<Object> getBeansWithAnnotation(String pluginId, Class<? extends Annotation> annotationType) {
        ApplicationContext pluginApplicationContext = pluginBeans.get(pluginId);
        if (pluginApplicationContext != null) {
            Map<String, Object> beanMap = pluginApplicationContext.getBeansWithAnnotation(annotationType);
            return new ArrayList<>(beanMap.values());
        }
        return new ArrayList<>(0);
    }
}
