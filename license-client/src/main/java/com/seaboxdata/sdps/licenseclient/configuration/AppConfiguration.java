package com.seaboxdata.sdps.licenseclient.configuration;


import com.seaboxdata.sdps.licenseclient.annotation.GoffConfiguration;

import com.seaboxdata.sdps.licenseclient.intf.Observer;
import com.seaboxdata.sdps.licenseclient.intf.Observerable;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 从配置中心加载的配置
 * 演示怎么从配置中心加载可变变量（注意数据库，缓存，MQ等，是固定的，不会因为配置变化而重新构建新实例，需要重启服务才行）
 * 这里的app.timeout等变量是不在本地配置里的，从配置中心获取
 *
 * @author stonehan
 */
@ConfigurationProperties(prefix = "app")
public class AppConfiguration implements Observerable, ApplicationContextAware{

    private final List<Observer<AppConfiguration>> observers = new ArrayList<>();

    //对应配置文件配置   app.timeout
    private int timeout;

    //对应配置文件配置   app.dataDir
    private String dataDir;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        notifyObserver();
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
        notifyObserver();
    }


    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObserver() {
        for (Observer<AppConfiguration> o : observers) {
            o.update(this);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//		因为配置变更的的观察者模式实现是泛型，获取的时候用泛型判断比较麻烦，使用annotation做判断
        Map<String, Object> obs = applicationContext.getBeansWithAnnotation(
                GoffConfiguration.class);
        obs.values().forEach(obj -> {
            if (obj instanceof Observer) {
                this.observers.add((Observer<AppConfiguration>) obj);
            }
        });
    }
}
