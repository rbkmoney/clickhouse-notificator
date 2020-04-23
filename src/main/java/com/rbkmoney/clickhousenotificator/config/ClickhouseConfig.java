package com.rbkmoney.clickhousenotificator.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseQueryParam;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class ClickhouseConfig {

    @Value("${clickhouse.db.url}")
    private String dbUrl;

    @Value("${clickhouse.db.user}")
    private String user;

    @Value("${clickhouse.db.password}")
    private String password;

    @Value("${clickhouse.db.connection.timeout}")
    private String connectionTimeout;

    @Value("${clickhouse.db.compress}")
    private String compress;

    @Bean
    @Qualifier("clickHouseDataSource")
    public ClickHouseDataSource clickHouseDataSource() {
        Properties info = new Properties();
        info.setProperty(ClickHouseQueryParam.USER.getKey(), user);
        info.setProperty(ClickHouseQueryParam.PASSWORD.getKey(), password);
        info.setProperty(ClickHouseQueryParam.COMPRESS.getKey(), compress);
        info.setProperty(ClickHouseQueryParam.CONNECT_TIMEOUT.getKey(), connectionTimeout);
        return new ClickHouseDataSource(dbUrl, info);
    }

    @Bean
    @Qualifier("jdbcTemplateCH")
    public JdbcTemplate jdbcTemplateCH(@Qualifier("clickHouseDataSource") DataSource clickHouseDataSource) {
        return new JdbcTemplate(clickHouseDataSource);
    }

}
