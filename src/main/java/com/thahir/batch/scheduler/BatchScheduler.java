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

    /**
     * Schedule the batch job to run every day at
     * The cron expression format is: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 55 11 * * ?")
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
