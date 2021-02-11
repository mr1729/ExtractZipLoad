package com.example.sai.ezl.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.sql.SQLException;

@Configuration
public class MysqlDataSource {

    @Value("${spring.hikari.datasource.mysql.username}")
    private String username;

    @Value("${spring.hikari.datasource.mysql.url}")
    private String url;

    @Value("${spring.hikari.datasource.mysql.password}")
    private String password;

    private DataSourceConfig dataSourceConfig;

    @Primary
    @Bean(name="DataSourceMysql")
    public HikariDataSource getMysqlHikariDataSource() throws SQLException {
        dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setJdbcUrl(url);
        dataSourceConfig.setUsername(username);
        dataSourceConfig.setPassword(password);
        return  dataSourceConfig.getDataSource();
    }

}
