package com.example.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class BackendApplication  {

    @Component
    private static class DefaultPluginApplication
            extends AbstractPluginApplication
            implements ApplicationContextAware, ApplicationListener<ApplicationStartedEvent>{

        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext  = applicationContext;
        }

        @Override
        public void onApplicationEvent(ApplicationStartedEvent event) {
            super.initialize(applicationContext);
        }
    }


    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
        System.out.println("sssss");
    }

}
