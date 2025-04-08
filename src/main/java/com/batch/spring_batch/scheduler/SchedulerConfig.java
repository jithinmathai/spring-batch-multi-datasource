package com.batch.spring_batch.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
    private final JobLauncher jobLauncher;
    private final Job updateOptInJob;

    public SchedulerConfig(JobLauncher jobLauncher, Job updateOptInJob) {
        this.jobLauncher = jobLauncher;
        this.updateOptInJob = updateOptInJob;
    }

    @Scheduled(fixedRate = 6000000)
    @Scheduled(cron = "* * * * * 0")// Run every minute
    public void runUpdateOptInJob() {
        try {
            jobLauncher.run(updateOptInJob,
                    new JobParametersBuilder()
                            .addLong("timestamp", System.currentTimeMillis())
                            .toJobParameters());
            logger.info("Job updateOptInJob executed successfully");
        } catch (Exception e) {
            logger.error("Error executing updateOptInJob", e);
        }
    }
}
