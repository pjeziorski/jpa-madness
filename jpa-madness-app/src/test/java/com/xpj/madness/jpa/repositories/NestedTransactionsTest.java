package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.services.NestedTransactionsService;
import com.xpj.madness.jpa.services.NestedTransactionsSubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@ActiveProfiles("legacy")
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class NestedTransactionsTest {

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    NestedTransactionsService nestedTransactionsService;

    @Autowired
    NestedTransactionsSubService nestedTransactionsSubService;

    @BeforeEach
    public void setUp() {
        newsRepository.deleteAll();

        nestedTransactionsService.setShouldThrowRuntimeException(false);
        nestedTransactionsService.setShouldThrowException(false);

        nestedTransactionsSubService.setShouldThrowRuntimeException(false);
        nestedTransactionsSubService.setShouldThrowException(false);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSave() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // save in single transaction
        nestedTransactionsService.transactionWithSave();

        assertThat(newsRepository.count()).isEqualTo(4);

        // break transaction on main service
        nestedTransactionsService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSave());

        assertThat(newsRepository.count()).isEqualTo(4);

        // break transaction on sub service
        nestedTransactionsService.setShouldThrowRuntimeException(false);
        nestedTransactionsSubService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSave());

        assertThat(newsRepository.count()).isEqualTo(4);

        // save one more time to be sure
        nestedTransactionsSubService.setShouldThrowRuntimeException(false);

        nestedTransactionsService.transactionWithSave();

        assertThat(newsRepository.count()).isEqualTo(8);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSaveAndFlush() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // save in single transaction
        nestedTransactionsService.transactionWithSaveAndFlush();

        assertThat(newsRepository.count()).isEqualTo(4);

        // break transaction on main service
        nestedTransactionsService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSaveAndFlush());

        assertThat(newsRepository.count()).isEqualTo(4);

        // break transaction on sub service
        nestedTransactionsService.setShouldThrowRuntimeException(false);
        nestedTransactionsSubService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSaveAndFlush());

        assertThat(newsRepository.count()).isEqualTo(4);

        // save one more time to be sure
        nestedTransactionsSubService.setShouldThrowRuntimeException(false);

        nestedTransactionsService.transactionWithSaveAndFlush();

        assertThat(newsRepository.count()).isEqualTo(8);
    }

    @Test
    public void shouldNotRollback_onCheckedException() {
        assertThat(newsRepository.count()).isEqualTo(0);

        // break transaction on main service
        nestedTransactionsService.setShouldThrowException(true);

        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSaveAndFlush());

        assertThat(newsRepository.count()).isEqualTo(3);

        // check using without flush
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSave());

        assertThat(newsRepository.count()).isEqualTo(6);
    }


}
