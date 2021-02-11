package com.example.sai.ezl.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
public class DataSourceConfig {
   private  HikariConfig hikariConfig = new HikariConfig();
   private HikariDataSource hikariDataSource;

   public HikariDataSource  getDataSource() throws SQLException {
       hikariConfig.addDataSourceProperty( "cachePrepStmts" , "true" );
       hikariConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
       hikariConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
       hikariDataSource = new HikariDataSource(hikariConfig);
       return hikariDataSource;
   }
   public void setJdbcUrl(String url){
       hikariConfig.setJdbcUrl(url);
   }
   public void setUsername(String username){
       hikariConfig.setUsername(username);
   }
   public void setPassword(String password){
       hikariConfig.setPassword(password);
   }
}
