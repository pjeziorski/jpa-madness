package com.xpj.madness.jpa.legacy;

import com.xpj.madness.jpa.legacy.repositories.NewsEntityManagerLogicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@ActiveProfiles("legacy")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class EntityManagerTransactionsTest {

    @Autowired
    NewsEntityManagerLogicRepository newsEntityManagerLogicRepository;

    @BeforeEach
    public void setUp() {
        newsEntityManagerLogicRepository.deleteAll();

        newsEntityManagerLogicRepository.shouldThrowRuntimeException.set(false);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSave() {
        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(0);

        // save in single transaction
        newsEntityManagerLogicRepository.transactionWithSave();

        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(2);

        // break transaction
        newsEntityManagerLogicRepository.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsEntityManagerLogicRepository.transactionWithSave());

        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(2);

        // save one more time to be sure
        newsEntityManagerLogicRepository.shouldThrowRuntimeException.set(false);

        newsEntityManagerLogicRepository.transactionWithSave();

        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(4);
    }

    @Test
    public void shouldSaveAll_orNothing_usingSaveAndFlush() {
        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(0);

        // save in single transaction
        newsEntityManagerLogicRepository.transactionWithSaveAndFlush();

        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(2);

        // break transaction
        newsEntityManagerLogicRepository.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsEntityManagerLogicRepository.transactionWithSaveAndFlush());

        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(2);

        // save one more time to be sure
        newsEntityManagerLogicRepository.shouldThrowRuntimeException.set(false);

        newsEntityManagerLogicRepository.transactionWithSaveAndFlush();

        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(4);
    }

    @Test
    public void shouldNotPersist_forPrivateMethod() {
        assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
                .isThrownBy(() -> newsEntityManagerLogicRepository.privateTransactionWithSaveAndFlush());
    }

    @Test
    @Transactional // to enable entityManager.persist()
    public void shouldSave_ignoringTransactional_forPrivateMethod() {
        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(0);

        // save in single transaction
        newsEntityManagerLogicRepository.privateTransactionWithSaveAndFlush();

        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(2);

        // break transaction
        newsEntityManagerLogicRepository.shouldThrowRuntimeException.set(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> newsEntityManagerLogicRepository.privateTransactionWithSaveAndFlush());

        // IMPORTANT!
        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(3);

        // save one more time to be sure
        newsEntityManagerLogicRepository.shouldThrowRuntimeException.set(false);

        newsEntityManagerLogicRepository.privateTransactionWithSaveAndFlush();

        // IMPORTANT!
        assertThat(newsEntityManagerLogicRepository.count()).isEqualTo(5);
    }

}
