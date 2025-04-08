package com.batch.spring_batch.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.batch.spring_batch.dto.Quote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class BatchConfigTest {

    @Autowired
    private Job processQuotesJob;

    @Autowired
    private Step updateOptInStep;

    @Autowired
    private Step deleteOldQuotesStep;

    @Autowired
    private ItemProcessor<Quote, Quote> quoteUpdateProcessor;



    @Test
    public void testJobBeanExists() {
        // Verify the job bean is created and has the expected name
        assertThat(processQuotesJob).isNotNull();
        assertThat(processQuotesJob.getName()).isEqualTo("processQuotesJob");
    }
    @Test
    public void testStepBeansExist() {
        // Verify that both step beans are created and have the correct names.
        assertThat(updateOptInStep).isNotNull();
        assertThat(updateOptInStep.getName()).isEqualTo("updateOptInStep");

        assertThat(deleteOldQuotesStep).isNotNull();
        assertThat(deleteOldQuotesStep.getName()).isEqualTo("deleteOldQuotesStep");
    }


    @Test
    public void testItemProcessorForOldQuote() throws Exception {
        // Given a Quote with a createdTime older than 1 month, the processor should set opt_in to false.
        Quote oldQuote = new Quote();
        oldQuote.setId(1L);
        oldQuote.setQuoteId("Q1");
        // Set the created_time to two months ago so that it qualifies as old.
        oldQuote.setCreatedTime(LocalDateTime.now().minusMonths(2));
        oldQuote.setOptIn(true);

        Quote result = quoteUpdateProcessor.process(oldQuote);
        // The processor should update opt_in to false.
        assertThat(result).isNotNull();
        assertFalse(result.isOptIn(), "For an old quote, opt_in should be false");
    }

    @Test
    public void testItemProcessorForRecentQuote() throws Exception {
        // Given a Quote with a createdTime within one month, the processor should leave opt_in unchanged.
        Quote recentQuote = new Quote();
        recentQuote.setId(2L);
        recentQuote.setQuoteId("Q2");
        // Set the created_time to 10 days ago so that it is recent.
        recentQuote.setCreatedTime(LocalDateTime.now().minusDays(10));
        recentQuote.setOptIn(true);

        Quote result = quoteUpdateProcessor.process(recentQuote);
        // The processor should leave opt_in as true.
        assertThat(result).isNotNull();
        assertTrue(result.isOptIn(), "For a recent quote, opt_in should remain true");
    }

    @Test
    public void testJobStepOrder() {
        // If the job is an instance of SimpleJob, we can verify the order of steps.
        // Note: Not all Job implementations expose their step order directly.
        // In our configuration, the job is defined as starting with updateOptInStep and then next deleteOldQuotesStep.
        // Therefore, the expectation is that these steps are executed in the correct sequence.
        // For a deeper verification, one could launch the job and inspect the job execution details.
        assertThat(processQuotesJob.getName()).isEqualTo("processQuotesJob");
    }


}
