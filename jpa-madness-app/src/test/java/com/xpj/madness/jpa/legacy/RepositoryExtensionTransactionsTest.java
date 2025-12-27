package com.xpj.madness.jpa.legacy;

import com.xpj.madness.jpa.legacy.repositories.NewsRepositoryLogicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@ActiveProfiles("legacy")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class RepositoryExtensionTransactionsTest {

    @Autowired
    NewsRepositoryLogicRepository repository;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();

        repository.shouldThrowRuntimeException.set(false);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSave() {
        assertThat(repository.count()).isEqualTo(0);

        // save in single transaction
        repository.transactionWithSave();

        assertThat(repository.count()).isEqualTo(2);

        // break transaction
        repository.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> repository.transactionWithSave());

        assertThat(repository.count()).isEqualTo(2);

        // save one more time to be sure
        repository.shouldThrowRuntimeException.set(false);

        repository.transactionWithSave();

        assertThat(repository.count()).isEqualTo(4);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSaveAndFlush() {
        assertThat(repository.count()).isEqualTo(0);

        // save in single transaction
        repository.transactionWithSaveAndFlush();

        assertThat(repository.count()).isEqualTo(2);

        // break transaction
        repository.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> repository.transactionWithSaveAndFlush());

        assertThat(repository.count()).isEqualTo(2);

        // save one more time to be sure
        repository.shouldThrowRuntimeException.set(false);

        repository.transactionWithSaveAndFlush();

        assertThat(repository.count()).isEqualTo(4);
    }

    @Test
    public void shouldSave_ignoringTransactional_forPrivateMethod() {
        assertThat(repository.count()).isEqualTo(0);

        // save in single transaction
        repository.privateTransactionWithSaveAndFlush();

        assertThat(repository.count()).isEqualTo(2);

        // break transaction
        repository.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> repository.privateTransactionWithSaveAndFlush());

        // IMPORTANT!
        assertThat(repository.count()).isEqualTo(3);

        // save one more time to be sure
        repository.shouldThrowRuntimeException.set(false);

        repository.privateTransactionWithSaveAndFlush();

        // IMPORTANT!
        assertThat(repository.count()).isEqualTo(5);
    }

}
