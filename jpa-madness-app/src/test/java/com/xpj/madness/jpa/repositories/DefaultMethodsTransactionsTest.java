package com.xpj.madness.jpa.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED) // enforces each call for separate transaction
public class DefaultMethodsTransactionsTest {

    @Autowired
    NewsRepositoryWithDefaultMethod newsRepositoryWithDefaultMethod;

    @BeforeEach
    public void setUp() {
        newsRepositoryWithDefaultMethod.deleteAll();

        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(false);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSave() {
        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(0);

        // save in single transaction
        newsRepositoryWithDefaultMethod.transactionWithSave();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(2);

        // break transaction
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsRepositoryWithDefaultMethod.transactionWithSave());

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(2);

        // save one more time to be sure
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(false);

        newsRepositoryWithDefaultMethod.transactionWithSave();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(4);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSaveAndFlush() {
        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(0);

        // save in single transaction
        newsRepositoryWithDefaultMethod.transactionWithSaveAndFlush();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(2);

        // break transaction
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsRepositoryWithDefaultMethod.transactionWithSaveAndFlush());

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(2);

        // save one more time to be sure
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(false);

        newsRepositoryWithDefaultMethod.transactionWithSaveAndFlush();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(4);
    }

    @Test
    public void shouldSaveAll_orNothing_usingTransientNonTransactionalMethod() {
        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(0);

        // save in single transaction
        newsRepositoryWithDefaultMethod.transactionalSaveAndFlushInNonTransactional();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(2);

        // break transaction
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsRepositoryWithDefaultMethod.transactionalSaveAndFlushInNonTransactional());

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(2);

        // save one more time to be sure
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(false);

        newsRepositoryWithDefaultMethod.transactionalSaveAndFlushInNonTransactional();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(4);
    }

    @Test
    public void shouldNotUseTransaction_usingSave() {
        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(0);

        // save in single transaction
        newsRepositoryWithDefaultMethod.nonTransactionalWithSave();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(2);

        // break transaction
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsRepositoryWithDefaultMethod.nonTransactionalWithSave());

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(3);

        // save one more time to be sure
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(false);

        newsRepositoryWithDefaultMethod.nonTransactionalWithSave();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(5);
    }

    @Test
    public void shouldNotUseTransaction_usingSaveAndFLush() {
        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(0);

        // save in single transaction
        newsRepositoryWithDefaultMethod.nonTransactionalWithSaveAndFlush();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(2);

        // break transaction
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsRepositoryWithDefaultMethod.nonTransactionalWithSaveAndFlush());

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(3);

        // save one more time to be sure
        newsRepositoryWithDefaultMethod.shouldThrowRuntimeException.set(false);

        newsRepositoryWithDefaultMethod.nonTransactionalWithSaveAndFlush();

        assertThat(newsRepositoryWithDefaultMethod.count()).isEqualTo(5);
    }

}
