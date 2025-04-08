package com.batch.spring_batch.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestBatchConfig.class)
@TestPropertySource(properties = {
        "spring.batch.job.enabled=false"
})
public class BatchJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;  // Already defined in your test context

    @Autowired
    private DataSourceInitializer dataSourceInitializer;

    @BeforeEach  // Use @Before to initialize before tests run
    public void setup() {
        // Ensure schema is initialized before job runs
        dataSourceInitializer.afterPropertiesSet();
    }


    public void testJob() throws Exception {
        // Now launch the job
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }
}