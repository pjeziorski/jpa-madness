package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.services.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED) // enforces each call for separate transaction
@ComponentScan("com.xpj.madness.jpa.services")
public class BasicTransactionsTest {

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    NewsService newsService;

    @BeforeEach
    public void setUp() {
        newsRepository.deleteAll();

        newsService.setShouldThrowRuntimeException(false);
        newsService.setShouldThrowException(false);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSave() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // save in single transaction
        newsService.transactionWithSave();

        assertThat(newsRepository.count()).isEqualTo(2);

        // break transaction
        newsService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsService.transactionWithSave());

        assertThat(newsRepository.count()).isEqualTo(2);

        // save one more time to be sure
        newsService.setShouldThrowRuntimeException(false);

        newsService.transactionWithSave();

        assertThat(newsRepository.count()).isEqualTo(4);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSaveAndFlush() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // save in single transaction
        newsService.transactionWithSaveAndFlush();

        assertThat(newsRepository.count()).isEqualTo(2);

        // break transaction
        newsService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsService.transactionWithSaveAndFlush());

        assertThat(newsRepository.count()).isEqualTo(2);

        // save one more time to be sure
        newsService.setShouldThrowRuntimeException(false);

        newsService.transactionWithSaveAndFlush();

        assertThat(newsRepository.count()).isEqualTo(4);
    }

    @Test
    public void shouldSave_ignoringTransactional_forPrivateMethod() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // save in single transaction
        newsService.privateTransactionWithSaveAndFlush();

        assertThat(newsRepository.count()).isEqualTo(2);

        // break transaction
        newsService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsService.privateTransactionWithSaveAndFlush());

        // IMPORTANT!
        assertThat(newsRepository.count()).isEqualTo(3);

        // save one more time to be sure
        newsService.setShouldThrowRuntimeException(false);

        newsService.privateTransactionWithSaveAndFlush();

        // IMPORTANT!
        assertThat(newsRepository.count()).isEqualTo(5);
    }

    @Test
    public void shouldSave_forNonTransactionalMethod() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // save in single transaction
        newsService.nonTransactionWithSave();

        assertThat(newsRepository.count()).isEqualTo(2);

        // break transaction
        newsService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsService.nonTransactionWithSave());

        // IMPORTANT!
        assertThat(newsRepository.count()).isEqualTo(3);

        // save one more time to be sure
        newsService.setShouldThrowRuntimeException(false);

        newsService.nonTransactionWithSave();

        // IMPORTANT!
        assertThat(newsRepository.count()).isEqualTo(5);
    }

    @Test
    public void shouldNotRollback_onCheckedException() {
        assertThat(newsRepository.count()).isEqualTo(0);

        newsService.setShouldThrowException(true);

        // using method save
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> newsService.transactionWithSave());

        assertThat(newsRepository.count()).isEqualTo(1);

        // using method saveAndFlush
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> newsService.transactionWithSaveAndFlush());

        assertThat(newsRepository.count()).isEqualTo(2);

        // using private transactional method
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> newsService.privateTransactionWithSaveAndFlush());

        assertThat(newsRepository.count()).isEqualTo(3);
    }
}
