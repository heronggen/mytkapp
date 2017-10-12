package org.tkapp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig implements TransactionManagementConfigurer {

    @Value("${datasource.postgres.drive-class-name}")
    String driveClassName;
    @Value("${datasource.postgres.jdbcUrl}")
    String jdbcUrl;
    @Value("${datasource.postgres.username}")
    String username;
    @Value("${datasource.postgres.password}")
    String password;
    @Value("${datasource.postgres.connectionTimeout}")
    long connectionTimeout;
    @Value("${datasource.postgres.idleTimeout}")
    long idleTimeout;
    @Value("${datasource.postgres.maxLifetime}")
    long maxLifetime;
    @Value("${datasource.postgres.maximumPoolSize}")
    int maximumPoolSize;
    @Value("${datasource.postgres.class-mapper}")
    String classMapper;

    @Bean
    @Primary
    public DataSource postgresDataSource(){
        return getDataSource(driveClassName,jdbcUrl,username,password,connectionTimeout,idleTimeout,maxLifetime,maximumPoolSize);
    }

    public HikariDataSource getDataSource(String driveClassName,String jdbcUrl,String username,String password,long connectionTimeout,long idleTimeout,long maxLifetime,int maximumPoolSize){
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driveClassName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setMaximumPoolSize(maximumPoolSize);
        HikariDataSource ds = new HikariDataSource(config);
        return  ds;
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(){
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(postgresDataSource());
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            bean.setMapperLocations(resolver.getResources(classMapper));
            return bean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(postgresDataSource());
        ChainedTransactionManager chainedTransactionManager = new ChainedTransactionManager(dataSourceTransactionManager);
        return chainedTransactionManager;
    }
}
