package com.thahir.batch.config;

import com.thahir.batch.listener.JobCompletionNotificationListener;
import com.thahir.batch.model.Customer;
import com.thahir.batch.model.FormattedCustomer;
import com.thahir.batch.processor.CustomerProcessor;
import com.thahir.batch.writer.ExcelWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {

    private final DataSource dataSource;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private ExcelWriter excelWriter;

    public BatchConfiguration(DataSource dataSource, JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;

    }

    @Bean
    public JdbcCursorItemReader<Customer> reader() {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerReader")
                .dataSource(dataSource)
                .sql("SELECT id, first_name, last_name, email, phone_number FROM customers")
                .rowMapper(customerRowMapper())
                .build();
    }

    @Bean
    public RowMapper<Customer> customerRowMapper() {
        return (rs, rowNum) -> {
            Customer customer = new Customer();
            customer.setId(rs.getLong("id"));
            customer.setFirstName(rs.getString("first_name"));
            customer.setLastName(rs.getString("last_name"));
            customer.setEmail(rs.getString("email"));
            customer.setPhoneNumber(rs.getString("phone_number"));
            return customer;
        };
    }

    @Bean
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }

    @Bean
    public ExcelWriter write() {
        if (excelWriter == null) {
            excelWriter = new ExcelWriter();
        }
        return excelWriter;
    }

    @Bean
    public Step processCustomersStep() {
        return new StepBuilder("processCustomersStep", jobRepository)
                .<Customer, FormattedCustomer>chunk(Integer.MAX_VALUE, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(write())
                .build();
    }

    @Bean
    public Job exportCustomerJob(JobCompletionNotificationListener listener, Step processCustomersStep) {
        return new JobBuilder("exportCustomerJob", jobRepository)
                .listener(listener)
                .start(processCustomersStep)
                .build();
    }
}
