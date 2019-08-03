package com.wkken.commonsvc.com.wkken.base;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Resource
public class DataSourceInit {

private static final Map<String, String> DbDriverMap;
static
{
    DbDriverMap = new HashMap<String, String>();
    DbDriverMap.put("jdbc:derby", "org.apache.derby.jdbc.EmbeddedDriver");
    DbDriverMap.put("jdbc:mysql", "com.mysql.jdbc.Driver");
    DbDriverMap.put("jdbc:oracle", "oracle.jdbc.driver.OracleDriver");
    DbDriverMap.put("jdbc:microsoft", "com.microsoft.jdbc.sqlserver.SQLServerDriver");
    DbDriverMap.put("jdbc:jtds", "net.sourceforge.jtds.jdbc.Driver");
    DbDriverMap.put("jdbc:postgresql", "org.postgresql.Driver");
    DbDriverMap.put("jdbc:sqlite", "org.sqlite.JDBC");
    DbDriverMap.put("jdbc:h2", "org.h2.Driver");

}


@Autowired
SlaveDataSourceConfig slaveDataSourceConfig;




private static final String DATASOURCE_TYPE_DEFAULT = "com.alibaba.druid.pool.DruidDataSource";

@Bean(name = "defaultDataSource")
@Qualifier("defaultDataSource")
@ConfigurationProperties(prefix = "spring.datasource.primary")
public DataSource primaryDataSource(){

    System.out.println(slaveDataSourceConfig.getDatasources());
    System.out.println("11111");
    return DruidDataSourceBuilder.create().build();
}


@Bean(name = "slaveDataSources")
@Qualifier("slaveDataSources")
public Map<String, DataSource> slaveDataSources (){
    Map<String, DataSource> slaveDataSources = new HashMap<>();

    for(String key:slaveDataSourceConfig.getDatasources().keySet()) {
        slaveDataSources.put(key, buildDataSource(slaveDataSourceConfig.getDatasources().get(key)));

    }

    return slaveDataSources;
}

public DataSource buildDataSource(Map<String, Object> dataSourceMap) {

    try {

        Object type = dataSourceMap.get("type");

        if (type == null) {

            type = DATASOURCE_TYPE_DEFAULT;// 默认DataSource

        }

        Class<? extends DataSource> dataSourceType;

        dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);



        String url = dataSourceMap.get("url").toString();


        String driverClassName="";
        if (dataSourceMap.containsKey("driver")) {
            driverClassName = dataSourceMap.get("driver").toString();
        }else
        {

            for(String key:DbDriverMap.keySet()) {
              if (url.indexOf(key)>0)
              {
                  driverClassName=DbDriverMap.get(key);
                  break;
              }

            }
        }


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
