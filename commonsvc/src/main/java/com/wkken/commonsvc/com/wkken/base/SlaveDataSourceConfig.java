package com.wkken.commonsvc.com.wkken.base;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
@Configuration
@ConfigurationProperties
public class SlaveDataSourceConfig {
public Map<String ,Map<String, Object>> getDatasources() {
    return datasources;
}

public void setDatasources(Map<String,Map<String, Object>> datasources) {
    this.datasources = datasources;
}
private Map<String,Map<String, Object>> datasources;


}
