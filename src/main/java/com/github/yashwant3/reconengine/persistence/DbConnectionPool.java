package com.github.yashwant3.reconengine.persistence;

import com.github.yashwant3.reconengine.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.hibernate.HikariConfigurationUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class DbConnectionPool {
    private static final HikariDataSource dataSource;

    static{
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppConfig.DB_URL);
        config.setUsername(AppConfig.DB_USER);
        config.setPassword(AppConfig.DB_PASSWORD);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10_000);

        dataSource= new HikariDataSource(config);
    }
    public static Connection getConnection() throws SQLException{
        return dataSource.getConnection();
    }
    public static void close(){
        if(dataSource != null && !dataSource.isClosed()){
            dataSource.close();
        }
    }
}
