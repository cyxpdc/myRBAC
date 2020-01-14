package com.pdc.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * ApplicationContext获取
 * 需要在spring-servlet.xml中配置为bean
 * ApplicationContextAware：https://blog.csdn.net/baidu_19473529/article/details/81072524
 * @author pdc
 */
@Component("applicationContextHelper")
public class ApplicationContextHelper implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if(applicationContext == null){//多加个判断，以免代码中调用此方法造成再次赋值
            applicationContext = context;
        }
    }
    /**
     * 从ApplicationContext获取bean
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) return null;
        return applicationContext.getBean(clazz);
    }
}
