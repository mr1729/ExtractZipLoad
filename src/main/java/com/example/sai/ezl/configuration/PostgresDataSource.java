package com.example.sai.ezl.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
public class PostgresDataSource {
    @Value("${spring.hikari.datasource.postgres.username}")
    private String username;

    @Value("${spring.hikari.datasource.postgres.jdbc-url}")
    private String url;

    @Value("${spring.hikari.datasource.postgres.password}")
    private String password;

    private DataSourceConfig dataSourceConfig;

    @Bean(name="DataSourcePostgres")
    public HikariDataSource getPostgresHikariDataSource() throws SQLException {
        dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setJdbcUrl(url);
        dataSourceConfig.setUsername(username);
        dataSourceConfig.setPassword(password);
        return  dataSourceConfig.getDataSource();
    }

}
