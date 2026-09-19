package com.github.yashwant3.reconengine.config;

public class AppConfig {
    public static final String DB_URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/reconciliation_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true"
    );
    public static final String DB_USER= System.getenv().getOrDefault("DB_USER","root");
    public static final String DB_PASSWORD= System.getenv().getOrDefault("DB_PASSWORD", "CHANGE_ME");
    // we need to change password with our mysql password when i want to push this file to github
}
