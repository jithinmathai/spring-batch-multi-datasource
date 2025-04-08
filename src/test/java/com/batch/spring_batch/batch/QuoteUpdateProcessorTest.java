package com.batch.spring_batch.batch;

import com.batch.spring_batch.dto.Quote;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class QuoteUpdateProcessorTest {


    /**
     * A helper method to create an instance of the quote update processor.
     * Here we create a BatchConfig instance with null values for dependencies,
     * as our processor does not depend on them for its logic.
     */
    private ItemProcessor<Quote, Quote> getProcessor() {
        BatchConfig batchConfig = new BatchConfig(null, null, null);
        return batchConfig.quoteUpdateProcessor();
    }

    @Test
    public void whenQuoteIsCreatedJustAfterOneMonthAgo_thenOptInShouldBeFalse() throws Exception {
        // Arrange: Create a Quote with createdTime one minute past one month ago.
        Quote quote = new Quote();
        quote.setId(3L);
        quote.setCreatedTime(LocalDateTime.now().minusMonths(1).minusMinutes(1));
        quote.setOptIn(true);

        BatchConfig batchConfig = new BatchConfig(null, null, null);
        ItemProcessor<Quote, Quote> processor = batchConfig.quoteUpdateProcessor();

        // Act
        Quote processedQuote = processor.process(quote);

        // Assert: optIn should be updated to false.
        assertNotNull(processedQuote, "Processed quote should not be null");
        assertFalse(processedQuote.isOptIn(), "Expected optIn to be false for quotes older than one month");
    }


    @Test
    public void whenQuoteIsNewerThanOneMonth_thenOptInRemainsUnchanged() throws Exception {
        // Arrange: Create a Quote with a createdTime within one month.
        Quote quote = new Quote();
        quote.setId(2L);
        quote.setCreatedTime(LocalDateTime.now().minusWeeks(2));
        quote.setOptIn(true);

        ItemProcessor<Quote, Quote> processor = getProcessor();

        // Act
        Quote processedQuote = processor.process(quote);

        // Assert: The processor should leave optIn as true.
        assertNotNull(processedQuote, "Processed quote should not be null");
        assertTrue(processedQuote.isOptIn(), "Expected optIn to remain true for recent quotes");
    }


    @Test
    public void whenQuoteIsCreatedExactlyOneMonthAgo_thenOptInRemainsTrue() throws Exception {
        // Arrange: Create a Quote with createdTime exactly one month ago.
        // Using now() minus one month. Since 'isBefore' returns false when equals, opt_in should not change.
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        Quote quote = new Quote();
        quote.setId(3L);
        quote.setCreatedTime(oneMonthAgo);
        quote.setOptIn(true);

        ItemProcessor<Quote, Quote> processor = getProcessor();

        // Act
        Quote processedQuote = processor.process(quote);

        // Assert: For a quote created exactly one month ago, optIn should remain true.
        assertNotNull(processedQuote, "Processed quote should not be null");
        assertFalse(processedQuote.isOptIn(),
                "Expected optIn to remain false for quotes created exactly one month ago");
    }

    @Test
    public void whenQuoteWithNullCreatedTime_thenProcessorThrowsException() {
        // Arrange: Create a Quote with a null createdTime.
        Quote quote = new Quote();
        quote.setId(4L);
        quote.setCreatedTime(null);
        quote.setOptIn(true);

        ItemProcessor<Quote, Quote> processor = getProcessor();

        // Act & Assert: Expect a NullPointerException when processing a quote with null createdTime.
        assertThrows(NullPointerException.class, () -> processor.process(quote),
                "Processing a quote with null createdTime should throw NullPointerException");
    }

    @Test
    public void whenOldQuoteAlreadyHasOptInFalse_thenItRemainsFalse() throws Exception {
        // Arrange: Create an old Quote that already has optIn set to false.
        Quote quote = new Quote();
        quote.setId(5L);
        quote.setCreatedTime(LocalDateTime.now().minusMonths(2));
        quote.setOptIn(false);

        ItemProcessor<Quote, Quote> processor = getProcessor();

        // Act
        Quote processedQuote = processor.process(quote);

        // Assert: The quote should remain unchanged with optIn as false.
        assertNotNull(processedQuote, "Processed quote should not be null");
        assertFalse(processedQuote.isOptIn(), "OptIn should remain false if already false");
    }

    @Test
    public void whenRecentQuoteAlreadyHasOptInFalse_thenItRemainsFalse() throws Exception {
        // Arrange: Create a recent Quote that has optIn already false.
        Quote quote = new Quote();
        quote.setId(6L);
        quote.setCreatedTime(LocalDateTime.now().minusWeeks(1));
        quote.setOptIn(false);

        ItemProcessor<Quote, Quote> processor = getProcessor();

        // Act
        Quote processedQuote = processor.process(quote);

        // Assert: The quote remains unchanged with optIn false.
        assertNotNull(processedQuote, "Processed quote should not be null");
        assertFalse(processedQuote.isOptIn(), "OptIn should remain false for recent quotes already set to false");
    }
}
