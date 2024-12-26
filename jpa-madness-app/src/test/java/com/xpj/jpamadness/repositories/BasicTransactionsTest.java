package com.xpj.jpamadness.repositories;

import com.xpj.jpamadness.services.ComplexLogicService;
import com.xpj.jpamadness.services.EnforcedTransactionsNewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@ComponentScan("com.xpj.jpamadness.services")
public class BasicTransactionsTest {

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    ComplexLogicService complexLogicService;

    @BeforeEach
    public void setUp() {
        newsRepository.deleteAll();

        complexLogicService.setShouldThrowRuntimeException(false);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSave() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // save in single transaction
        complexLogicService.transactionWithSave();

        assertThat(newsRepository.count()).isEqualTo(2);

        // break transaction
        complexLogicService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> complexLogicService.transactionWithSave());

        assertThat(newsRepository.count()).isEqualTo(2);

        // save one more time to be sure
        complexLogicService.setShouldThrowRuntimeException(false);

        complexLogicService.transactionWithSave();

        assertThat(newsRepository.count()).isEqualTo(4);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSaveAndFlush() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // save in single transaction
        complexLogicService.transactionWithSaveAndFlush();

        assertThat(newsRepository.count()).isEqualTo(2);

        // break transaction
        complexLogicService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> complexLogicService.transactionWithSaveAndFlush());

        assertThat(newsRepository.count()).isEqualTo(2);

        // save one more time to be sure
        complexLogicService.setShouldThrowRuntimeException(false);

        complexLogicService.transactionWithSaveAndFlush();

        assertThat(newsRepository.count()).isEqualTo(4);
    }

    @Test
    public void shouldSaveAll_ignoringTransactional_forPrivateMethod() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // save in single transaction
        complexLogicService.privateTransactionWithSaveAndFlush();

        assertThat(newsRepository.count()).isEqualTo(2);

        // break transaction
        complexLogicService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> complexLogicService.privateTransactionWithSaveAndFlush());

        // IMPORTANT!
        assertThat(newsRepository.count()).isEqualTo(3);

        // save one more time to be sure
        complexLogicService.setShouldThrowRuntimeException(false);

        complexLogicService.privateTransactionWithSaveAndFlush();

        // IMPORTANT!
        assertThat(newsRepository.count()).isEqualTo(5);
    }

}
