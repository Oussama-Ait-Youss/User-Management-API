package com.usermanagementapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.usermanagementapi.repository")
@PropertySource("classpath:application.properties")
public class PersistenceConfig {

    private final Environment env;

    public PersistenceConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        String jdbcUrl = env.resolveRequiredPlaceholders(env.getRequiredProperty("db.url"));
        if (jdbcUrl.startsWith("jdbc:h2:file:")) {
            String databaseDirectory = env.resolveRequiredPlaceholders(
                    env.getRequiredProperty("db.directory"));
            try {
                Files.createDirectories(Path.of(databaseDirectory));
            } catch (IOException exception) {
                throw new IllegalStateException(
                        "Unable to create the H2 database directory: " + databaseDirectory, exception);
            }
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getRequiredProperty("db.driverClassName"));
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(env.getRequiredProperty("db.username"));
        config.setPassword(env.getProperty("db.password", ""));
        config.setMaximumPoolSize(env.getProperty("hikari.maximumPoolSize", Integer.class, 10));
        config.setMinimumIdle(env.getProperty("hikari.minimumIdle", Integer.class, 2));
        config.setIdleTimeout(env.getProperty("hikari.idleTimeout", Long.class, 30000L));
        config.setPoolName(env.getProperty("hikari.poolName", "UserManagementHikariPool"));
        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory =
                new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan("com.usermanagementapi.model");
        entityManagerFactory.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", env.getRequiredProperty("hibernate.dialect"));
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getRequiredProperty("hibernate.hbm2ddl.auto"));
        jpaProperties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql", "false"));
        jpaProperties.put("hibernate.format_sql", env.getProperty("hibernate.format_sql", "false"));
        entityManagerFactory.setJpaProperties(jpaProperties);
        return entityManagerFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
