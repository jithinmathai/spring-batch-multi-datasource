package com.batch.spring_batch.batch;

import com.batch.spring_batch.dto.Quote;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    Logger log = LoggerFactory.getLogger(BatchConfig.class);

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource batchDataSource;

    public BatchConfig(JobRepository jobRepository,
                       PlatformTransactionManager transactionManager,
                       @Qualifier("batchDataSource") DataSource batchDataSource) {
        log.info("Batch DataSource in use: {}", batchDataSource);
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.batchDataSource = batchDataSource;
    }

    // -----------------------------------------------------------------------
    // Step 1: Update step
    // (Update records created between twoYearsAgo and oneMonthAgo: set opt_in=false)
    // -----------------------------------------------------------------------
    @Bean
    public JdbcPagingItemReader<Quote> quoteReaderForUpdate() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("twoYearsAgo", LocalDateTime.now().minusYears(2));
        params.put("oneMonthAgo", LocalDateTime.now().minusMonths(1));
        params.put("optIn", true);

        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("SELECT id, quote_id, basic_quote_id, customer_name, customer_email, customer_phone, request_text, created_time, opt_in");
        queryProvider.setFromClause("FROM quote");
        // Use BETWEEN to restrict to records between two years and one month ago
        queryProvider.setWhereClause("WHERE created_time BETWEEN :twoYearsAgo AND :oneMonthAgo AND opt_in = :optIn");
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        log.debug("Executing update query with parameters: {}", params);

        return new JdbcPagingItemReaderBuilder<Quote>()
                .name("quoteReaderForUpdate")
                .dataSource(batchDataSource)
                .queryProvider(queryProvider)
                .parameterValues(params)
                .rowMapper((rs, rowNum) -> {
                    Quote q = new Quote();
                    q.setId(rs.getLong("id"));
                    q.setQuoteId(rs.getString("quote_id"));
                    q.setBasicQuoteId(rs.getString("basic_quote_id"));
                    q.setCustomerName(rs.getString("customer_name"));
                    q.setCustomerEmail(rs.getString("customer_email"));
                    q.setCustomerPhone(rs.getString("customer_phone"));
                    q.setRequestText(rs.getString("request_text"));
                    q.setCreatedTime(rs.getTimestamp("created_time").toLocalDateTime());
                    q.setOptIn(rs.getBoolean("opt_in"));
                    return q;
                })
                .pageSize(50)
                .build();
    }

    @Bean
    public ItemProcessor<Quote, Quote> quoteUpdateProcessor() {
        return quote -> {
            if (quote.getCreatedTime().isBefore(LocalDateTime.now().minusMonths(1))) {
                log.debug("Updating opt_in to false for Quote ID: {}", quote.getId());
                quote.setOptIn(false);
            }
            return quote;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Quote> quoteUpdateWriter() {
        String sql = "UPDATE quote SET opt_in = :optIn WHERE id = :id";
        log.debug("Update SQL: {}", sql);
        return new JdbcBatchItemWriterBuilder<Quote>()
                .dataSource(batchDataSource)
                .sql(sql)
                .beanMapped()
                .build();
    }

    // -----------------------------------------------------------------------
    // Step 2: Delete step
    // (Delete records older than 2 years)
    // -----------------------------------------------------------------------
    @Bean
    public JdbcPagingItemReader<Quote> quoteReaderForDelete() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("twoYearsAgo", LocalDateTime.now().minusYears(2));

        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        // Only selecting id is enough for deletion
        queryProvider.setSelectClause("SELECT id");
        queryProvider.setFromClause("FROM quote");
        queryProvider.setWhereClause("WHERE created_time < :twoYearsAgo");
        Map<String, Order> sortKeys = Collections.singletonMap("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        log.debug("Executing delete query with parameters: {}", params);

        return new JdbcPagingItemReaderBuilder<Quote>()
                .name("quoteReaderForDelete")
                .dataSource(batchDataSource)
                .queryProvider(queryProvider)
                .parameterValues(params)
                .rowMapper((rs, rowNum) -> {
                    Quote q = new Quote();
                    q.setId(rs.getLong("id"));
                    return q;
                })
                .pageSize(50)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Quote> quoteDeleteWriter() {
        String sql = "DELETE FROM quote WHERE id = :id";
        log.debug("Delete SQL: {}", sql);
        return new JdbcBatchItemWriterBuilder<Quote>()
                .dataSource(batchDataSource)
                .sql(sql)
                .beanMapped()
                .build();
    }

    // -----------------------------------------------------------------------
    // Step Execution Listener for logging counts
    // -----------------------------------------------------------------------
    @Bean
    public StepExecutionListener loggingListener() {
        return new StepExecutionListener() {
            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                log.info("Step [{}] processed {} items.", stepExecution.getStepName(), stepExecution.getWriteCount());
                return stepExecution.getExitStatus();
            }
        };
    }

    @Bean
    public ItemWriter<Quote> loggingItemWriter() {
        return items -> items.forEach(quote ->
                log.info("Deleting Quote - ID: {}, Quote ID: {}, Created Time: {}",
                        quote.getId(), quote.getQuoteId(), quote.getCreatedTime())
        );
    }

    @Bean
    public ItemWriter<Quote> compositeDeleteWriter(@Qualifier("quoteDeleteWriter") JdbcBatchItemWriter<Quote> deleteWriter) {
        CompositeItemWriter<Quote> compositeWriter = new CompositeItemWriter<>();

        // Create a list of delegates: first log, then delete
        compositeWriter.setDelegates(Arrays.asList(loggingItemWriter(), deleteWriter));

        return compositeWriter;
    }


    // -----------------------------------------------------------------------
    // Define the steps and the job
    // -----------------------------------------------------------------------
    @Bean
    public Step updateOptInStep() throws Exception {
        return new StepBuilder("updateOptInStep", jobRepository)
                .<Quote, Quote>chunk(50, transactionManager)
                .reader(quoteReaderForUpdate())
                .processor(quoteUpdateProcessor())
                .writer(quoteUpdateWriter())
                .listener(loggingListener())
                .build();
    }

    @Bean
    public Step deleteOldQuotesStep() throws Exception {
        return new StepBuilder("deleteOldQuotesStep", jobRepository)
                .<Quote, Quote>chunk(50, transactionManager)
                .reader(quoteReaderForDelete())
                .writer(compositeDeleteWriter(quoteDeleteWriter()))
                .listener(loggingListener())
                .build();
    }

    @Bean
    public Job processQuotesJob() throws Exception {
        return new JobBuilder("processQuotesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(updateOptInStep())
                .next(deleteOldQuotesStep())
                .build();
    }
}
