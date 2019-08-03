package com.wkken.commonsvc.com.wkken.base;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar ,EnvironmentAware{

private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceRegister.class);

private static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";

//默认数据源
@Autowired
private DataSource defaultDataSource;

//用户自定义数据源
@Autowired
private Map<String, DataSource> slaveDataSources;

@Override
public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
    Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
    targetDataSources.put("dataSource", this.defaultDataSource);

    DynamicDataSourceContextHolder.dataSourceIds.add("dataSource");

    targetDataSources.putAll(slaveDataSources);

    for (String key : slaveDataSources.keySet()) {

        DynamicDataSourceContextHolder.dataSourceIds.add(key);

    }
    //创建DynamicDataSource

    GenericBeanDefinition beanDefinition = new GenericBeanDefinition();

    beanDefinition.setBeanClass(DynamicDataSource.class);

    beanDefinition.setSynthetic(true);

    MutablePropertyValues mpv = beanDefinition.getPropertyValues();

    mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);

    mpv.addPropertyValue("targetDataSources", targetDataSources);

    //注册 - BeanDefinitionRegistry

    beanDefinitionRegistry.registerBeanDefinition("dataSource", beanDefinition);



    logger.info("Dynamic DataSource Registry");
}

@Override
public void setEnvironment(Environment environment) {
    System.out.println("222");
//    initDefaultDataSource();

 //   initslaveDataSources();
}
private void initDefaultDataSource() {

    // 读取主数据源
    defaultDataSource = DruidDataSourceBuilder.create().build();

}

private void initslaveDataSources() {

    // 读取配置文件获取更多数据源

  /*  for (Map<String, Object> dsConf:datasources)
    {
        slaveDataSources.put(dsConf.get("name").toString(), buildDataSource(dsConf));
    }*/
}
public DataSource buildDataSource(Map<String, Object> dataSourceMap) {

    try {

        Object type = dataSourceMap.get("type");

        if (type == null) {

            type = DATASOURCE_TYPE_DEFAULT;// 默认DataSource

        }

        Class<? extends DataSource> dataSourceType;

        dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);

        String driverClassName = dataSourceMap.get("driver").toString();

        String url = dataSourceMap.get("url").toString();

        String username = dataSourceMap.get("username").toString();

        String password = dataSourceMap.get("password").toString();

        // 自定义DataSource配置

        DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url)

                .username(username).password(password).type(dataSourceType);

        return factory.build();

    } catch (ClassNotFoundException e) {

        e.printStackTrace();

    }

    return null;

}

}
