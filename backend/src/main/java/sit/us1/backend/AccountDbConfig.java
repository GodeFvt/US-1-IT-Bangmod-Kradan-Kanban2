package sit.us1.backend;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;

@Configuration
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories(entityManagerFactoryRef = "AccountEntityManagerFactory", transactionManagerRef = "AccountTransactionManager", basePackages = {"sit.us1.backend.repositories.account"})
public class AccountDbConfig {

    @Bean(name = "accountDataSource")
    @ConfigurationProperties(prefix = "account.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "AccountEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean AccountEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("accountDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("sit.us1.backend.entities.account").persistenceUnit("account").build();
    }

    @Bean(name = "AccountTransactionManager")
    public PlatformTransactionManager AccountTransactionManager(@Qualifier("AccountEntityManagerFactory") EntityManagerFactory AccountEntityManagerFactory) {
        return new JpaTransactionManager(AccountEntityManagerFactory);
    }
}