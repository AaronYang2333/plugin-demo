package com.example.backend.manager;

import cn.hutool.core.lang.JarClassLoader;
import cn.hutool.core.util.ReflectUtil;
import com.example.backend.configuration.PluginAutoConfiguration;
import com.example.backend.entity.PluginInfo;
import com.example.backend.exception.PluginException;
import com.example.backend.util.DeployUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
@Slf4j
public class PluginClassRegister {

    private final ApplicationContext applicationContext;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final Method getMappingForMethod;
    private final Map<String, ApplicationContext> pluginBeans;

    private final Map<String, Set<RequestMappingInfo>> requestMappings = new ConcurrentHashMap<>();


    public PluginClassRegister(ApplicationContext applicationContext, PluginAutoConfiguration configuration, Map<String, ApplicationContext> pluginBeans) {
        this.applicationContext = applicationContext;
        this.requestMappingHandlerMapping = getRequestMapping();
        this.getMappingForMethod = getRequestMethod();
        this.pluginBeans = pluginBeans;
    }


    public ApplicationContext register(PluginInfo pluginInfo) {
        ApplicationContext pluginApplicationContext =  registerBean(pluginInfo);
        pluginBeans.put(pluginInfo.getId(), pluginApplicationContext);
        return pluginApplicationContext;
    }

    public boolean unRegister(PluginInfo pluginInfo) {
        return unRegisterBean(pluginInfo);
    }

    private boolean unRegisterBean(PluginInfo pluginInfo) {
        GenericWebApplicationContext pluginApplicationContext = (GenericWebApplicationContext) pluginBeans.get(pluginInfo.getId());
        pluginApplicationContext.close();
        //取消注册controller
        Set<RequestMappingInfo> requestMappingInfoSet = requestMappings.get(pluginInfo.getId());
        if (requestMappingInfoSet != null) {
            requestMappingInfoSet.forEach(this::unRegisterController);
        }
        requestMappings.remove(pluginInfo.getId());
        pluginBeans.remove(pluginInfo.getId());
        return true;
    }

    private void unRegisterController(RequestMappingInfo requestMappingInfo) {
        requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
    }

    private ApplicationContext registerBean(PluginInfo pluginInfo) {
        String path = pluginInfo.getPath();
        Set<String> classNames = DeployUtils.readClassFile(path);
        URLClassLoader classLoader = null;
        try {
            //class 加载器
            final URL[] urls = { new URL("jar:file:" + path + "!/") };
            classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

            //一个插件创建一个applicationContext
            GenericWebApplicationContext pluginApplicationContext = new GenericWebApplicationContext();
            pluginApplicationContext.setResourceLoader(new DefaultResourceLoader(classLoader));

            //plugin its own classloader
//            GenericWebApplicationContext pluginDependencyContext = new GenericWebApplicationContext();
//            JarClassLoader jarClassLoader = new JarClassLoader(urls, classLoader);
//            pluginDependencyContext.setResourceLoader(new DefaultResourceLoader(jarClassLoader));

            //注册bean
            List<String> beanNames = new ArrayList<>();
            List<String> dependencyNames = new ArrayList<>();
            for (String className : classNames) {
                Class clazz = classLoader.loadClass(className);
                System.out.println(className);
                if (DeployUtils.isSpringBeanClass(clazz)) {
                    String simpleClassName = DeployUtils.transformName(className);

                    BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) pluginApplicationContext.getBeanFactory();
                    BeanDefinitionBuilder usersBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                    usersBeanDefinitionBuilder.setScope(ConfigurableBeanFactory.SCOPE_SINGLETON);
                    beanDefinitionRegistry.registerBeanDefinition(simpleClassName, usersBeanDefinitionBuilder.getRawBeanDefinition());

                    beanNames.add(simpleClassName);
                }else {
                    String simpleClassName = DeployUtils.transformName(className);
                    BeanDefinitionRegistry beanDefinitonRegistry = (BeanDefinitionRegistry) pluginApplicationContext.getBeanFactory();
                    BeanDefinitionBuilder usersBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                    usersBeanDefinitionBuilder.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
                    beanDefinitonRegistry.registerBeanDefinition(className, usersBeanDefinitionBuilder.getRawBeanDefinition());

                    dependencyNames.add(simpleClassName);
                }
            }
            //刷新上下文
            pluginApplicationContext.refresh();
            //注入bean和注册接口
            Set<RequestMappingInfo> pluginRequestMappings = new HashSet<>();
            for (String beanName : beanNames) {
                //注入bean
                Object bean = pluginApplicationContext.getBean(beanName);
                injectService(bean);
                //注册接口
                Set<RequestMappingInfo> requestMappingInfos = registerController(bean);
                requestMappingInfos.forEach(requestMappingInfo -> {
                    log.info("插件{}注册接口{}", pluginInfo.getId(), requestMappingInfo);
                });
                pluginRequestMappings.addAll(requestMappingInfos);
            }
            requestMappings.put(pluginInfo.getId(), pluginRequestMappings);

            return pluginApplicationContext;
        } catch (Exception e) {
            throw new PluginException("注册bean异常", e);
        } finally {
            try {
                if (classLoader != null) {
                    classLoader.close();
                }
            } catch (IOException e) {
                log.error("classLoader关闭失败", e);
            }
        }
    }

    private Set<RequestMappingInfo> registerController(Object bean) {
        Class<?> aClass = bean.getClass();
        Set<RequestMappingInfo> requestMappingInfos = new HashSet<>();
        if (Boolean.TRUE.equals(DeployUtils.isController(aClass))) {
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                if (DeployUtils.isHaveRequestMapping(method)) {
                    try {
                        RequestMappingInfo requestMappingInfo = (RequestMappingInfo)
                                getMappingForMethod.invoke(requestMappingHandlerMapping, method, aClass);
                        requestMappingHandlerMapping.registerMapping(requestMappingInfo, bean, method);
                        requestMappingInfos.add(requestMappingInfo);
                    } catch (Exception e){
                        log.error("接口注册异常", e);
                    }
                }
            }
        }
        return requestMappingInfos;
    }


    private void injectService(Object instance){
        if (instance==null) {
            return;
        }

        Field[] fields = ReflectUtil.getFields(instance.getClass()); //instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Object fieldBean = null;
            // with bean-id, bean could be found by both @Resource and @Autowired, or bean could only be found by @Autowired

            if (AnnotationUtils.getAnnotation(field, Resource.class) != null) {
                try {
                    Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
                    if (resource.name()!=null && resource.name().length()>0){
                        fieldBean = applicationContext.getBean(resource.name());
                    } else {
                        fieldBean = applicationContext.getBean(field.getName());
                    }
                } catch (Exception e) {
                }
                if (fieldBean==null ) {
                    fieldBean = applicationContext.getBean(field.getType());
                }
            } else if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
                Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
                if (qualifier!=null && qualifier.value()!=null && qualifier.value().length()>0) {
                    fieldBean = applicationContext.getBean(qualifier.value());
                } else {
                    fieldBean = applicationContext.getBean(field.getType());
                }
            }

            if (fieldBean!=null) {
                field.setAccessible(true);
                try {
                    field.set(instance, fieldBean);
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private Method getRequestMethod() {
        try {
            Method method =  ReflectUtils.findDeclaredMethod(requestMappingHandlerMapping.getClass(), "getMappingForMethod", new Class[] { Method.class, Class.class });
            method.setAccessible(true);
            return method;
        } catch (Exception ex) {
            log.error("反射获取detectHandlerMethods异常", ex);
        }
        return null;
    }

    private RequestMappingHandlerMapping getRequestMapping() {
        return (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
    }

}

