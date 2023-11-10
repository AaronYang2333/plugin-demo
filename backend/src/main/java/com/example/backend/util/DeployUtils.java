package com.example.backend.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.example.backend.configuration.PluginConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.mapper.Mapper;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
@Slf4j
public class DeployUtils {

    public static Set<String> readJarFile(String jarAddress) {
        Set<String> classNameSet = new HashSet<>();

        try(JarFile jarFile = new JarFile(jarAddress)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (!name.startsWith(PluginConstants.META_INF_PREFIX)) {
                    if (name.endsWith(PluginConstants.CLASS_SUFFIX)) {
                        String className = name.replace(PluginConstants.CLASS_SUFFIX, "").replaceAll("/", ".");
                        classNameSet.add(className);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("加载jar包失败", e);
        }
        return classNameSet;
    }

    public static InputStream readManifestJarFile(File jarAddress) {
        try {
            JarFile jarFile = new JarFile(jarAddress);

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (name.contains(PluginConstants.MANIFEST)) {
                    return jarFile.getInputStream(jarEntry);
                }
            }
        } catch (Exception e) {
            log.warn("加载jar包失败", e);
        }
        return null;
    }

    /**
     * 方法描述 判断class对象是否带有spring的注解
     */
    public static boolean isSpringBeanClass(Class<?> cls) {
        if (cls == null) {
            return false;
        }
        //是否是接口
        if (cls.isInterface()) {
            return false;
        }
        //是否是抽象类
        if (Modifier.isAbstract(cls.getModifiers())) {
            return false;
        }
        if (cls.getAnnotation(Component.class) != null) {
            return true;
        }
        if (cls.getAnnotation(Service.class) != null) {
            return true;
        }
        if (cls.getAnnotation(RestController.class) != null) {
            return true;
        }
        return false;
    }


    public static boolean isController(Class<?> cls) {
        if (cls.getAnnotation(Controller.class) != null) {
            return true;
        }
        if (cls.getAnnotation(RestController.class) != null) {
            return true;
        }
        return false;
    }

    public static boolean isHaveRequestMapping(Method method) {
        return AnnotationUtils.findAnnotation(method, RequestMapping.class) != null;
    }

    /**
     * 类名首字母小写 作为spring容器beanMap的key
     */
    public static String transformName(String className) {
        String tmpstr = className.substring(className.lastIndexOf(".") + 1);
        return tmpstr.substring(0, 1).toLowerCase() + tmpstr.substring(1);
    }

    /**
     * 读取class文件
     * @param path
     * @return
     */
    public static Set<String> readClassFile(String path) {
        if (path.endsWith(PluginConstants.JAR_SUFFIX)) {
            return readJarFile(path);
        } else {
            List<File> pomFiles =  FileUtil.loopFiles(path, file -> file.getName().endsWith(PluginConstants.CLASS_SUFFIX));
            Set<String> classNameSet = new HashSet<>();
            for (File file : pomFiles) {
                String className = CharSequenceUtil.subBetween(file.getPath(), PluginConstants.CLASSES + File.separator, PluginConstants.CLASS_SUFFIX).replace(File.separator, ".");
                classNameSet.add(className);
            }
            return classNameSet;
        }
    }
}
