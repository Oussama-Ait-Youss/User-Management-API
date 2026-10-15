package com.usermanagementapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.usermanagement.repository")
@PropertySource("classpath:application.properties")
public class PersistenceConfig {

    private final Environment env;

    @Autowired
    public PersistenceConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getRequiredProperty("db.driverClassName"));
        config.setJdbcUrl(env.getRequiredProperty("db.url"));
        config.setUsername(env.getProperty("db.username", ""));
        config.setPassword(env.getProperty("db.password", ""));

        config.setMaximumPoolSize(env.getProperty("hikari.maximumPoolSize", Integer.class, 10));
        config.setMinimumIdle(env.getProperty("hikari.minimumIdle", Integer.class, 2));
        config.setIdleTimeout(env.getProperty("hikari.idleTimeout", Long.class, 30000L));
        config.setPoolName(env.getProperty("hikari.poolName", "PostgresHikariPool"));

        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.usermanagement.entity");
        em.setPersistenceProviderClass(HibernatePersistenceProvider.class);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", env.getRequiredProperty("hibernate.dialect"));
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getRequiredProperty("hibernate.hbm2ddl.auto"));
        jpaProperties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql", "false"));
        jpaProperties.put("hibernate.format_sql", env.getProperty("hibernate.format_sql", "false"));
        em.setJpaProperties(jpaProperties);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}