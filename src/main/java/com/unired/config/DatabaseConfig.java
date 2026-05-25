package com.unired.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfig {

    private static final String JDBC_URL =
            "jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres"
                    + "?sslmode=require"
                    + "&prepareThreshold=0"
                    + "&preparedStatementCacheQueries=0"
                    + "&preparedStatementCacheSizeMiB=0"
                    + "&preferQueryMode=simple";

    private static final String DB_USER = "postgres.ujfrazuolkhggzjgvogu";

    @Bean
    @Primary
    public DataSource dataSource(@Value("${DB_PASSWORD}") String dbPassword) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setJdbcUrl(JDBC_URL);
        hikariConfig.setUsername(DB_USER);
        hikariConfig.setPassword(dbPassword);
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setConnectionTimeout(20000);
        hikariConfig.addDataSourceProperty("prepareThreshold", "0");
        hikariConfig.addDataSourceProperty("preparedStatementCacheQueries", "0");
        hikariConfig.addDataSourceProperty("preparedStatementCacheSizeMiB", "0");
        hikariConfig.addDataSourceProperty("preferQueryMode", "simple");
        hikariConfig.addDataSourceProperty("sslmode", "require");
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    public org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> flyway.migrate();
    }
}
