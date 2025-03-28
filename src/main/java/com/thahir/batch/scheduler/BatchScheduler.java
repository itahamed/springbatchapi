package com.thahir.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job exportCustomerJob;

    //@Scheduled(cron = "0 0 1 1 * *") // run every 1st of the month at 1:00 AM
    @Scheduled(cron = "0 24 12 * * ?") // run every day at 11:55 AM
    public void runBatchJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(exportCustomerJob, jobParameters);
        } catch (Exception e) {
            System.err.println("Error occurred while executing batch job: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
