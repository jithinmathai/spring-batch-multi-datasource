package com.batch.spring_batch.batch;

import jakarta.annotation.PostConstruct;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
@TestConfiguration
public class TestBatchConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void loadTestData() {
        try (Connection conn = dataSource.getConnection()) {
            // Just load test data - table already exists
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("data.sql"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}