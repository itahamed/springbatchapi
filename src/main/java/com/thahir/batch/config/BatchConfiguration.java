package com.thahir.batch.config;

import com.thahir.batch.listener.JobCompletionNotificationListener;
import com.thahir.batch.model.Customer;
import com.thahir.batch.model.FormattedCustomer;
import com.thahir.batch.processor.CustomerProcessor;
import com.thahir.batch.writer.ExcelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

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
    public ExcelWriter writer() {
        return new ExcelWriter();
    }

    @Bean
    public Step processCustomersStep() {
        return stepBuilderFactory.get("processCustomersStep")
                .<Customer, FormattedCustomer>chunk(Integer.MAX_VALUE)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job exportCustomerJob(Step processCustomersStep) {
        return jobBuilderFactory.get("exportCustomerJob")
                .start(processCustomersStep)
                .build();
    }
}