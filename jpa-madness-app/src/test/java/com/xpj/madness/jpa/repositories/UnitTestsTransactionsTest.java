package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.Basket;
import com.xpj.madness.jpa.utils.HibernateStatistics;
import com.xpj.madness.jpa.services.NestedTransactionsService;
import com.xpj.madness.jpa.services.NestedTransactionsSubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@ActiveProfiles("legacy")
@ComponentScan("com.xpj.madness.jpa.services")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Import(HibernateStatistics.class)
public class UnitTestsTransactionsTest {

    @Autowired
    BasketRepository basketRepository;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    NestedTransactionsService nestedTransactionsService;

    @Autowired
    NestedTransactionsSubService nestedTransactionsSubService;

    @Autowired
    HibernateStatistics hibernateStatistics;

    @BeforeEach
    public void beforeEach() {
        System.out.println("Initial query count: " + hibernateStatistics.getQueryCount());
    }

    @Test
    public void shouldUseSingleTransaction_byDefault() {
        long expectedQueryCount = hibernateStatistics.getQueryCount();

        Basket basket = Basket.builder().build();

        basketRepository.save(basket);

        Basket returnedBasket = basketRepository.findById(basket.getId()).get();

        assertThat(returnedBasket).isEqualTo(basket);

        // no queries were needed
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);
    }

    @Test
    public void shouldUseSingleTransaction_byDefault_withSaveAndFlush() {
        long expectedQueryCount = hibernateStatistics.getQueryCount();

        Basket basket = Basket.builder().build();

        basketRepository.saveAndFlush(basket);

        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);

        Basket returnedBasket = basketRepository.findById(basket.getId()).get();

        assertThat(returnedBasket).isEqualTo(basket);

        // no new queries were needed
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);
    }

    // https://stackoverflow.com/questions/27987097/disabling-transaction-on-spring-testing-test-method
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldDisableUnitTestTransaction_andMakeTransactionsIndependent() {
        long expectedQueryCount = hibernateStatistics.getQueryCount();

        Basket basket = Basket.builder().build();

        basketRepository.save(basket);

        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);

        Basket returnedBasket = basketRepository.findById(basket.getId()).get();

        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);

        assertThat(basket.getItems()).isNull();
        assertThat(returnedBasket.getItems()).isNotNull();

        assertThat(returnedBasket)
                .usingRecursiveComparison()
                .ignoringFields("items")
                .isEqualTo(basket);

        // no queries were needed
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);
    }

    @Test
    public void shouldUseSingleTransaction_forNestedTransactions() {
        long expectedNewsCount = newsRepository.count();

        // save in single transaction
        nestedTransactionsService.transactionWithSaveAndFlush();

        expectedNewsCount += 4;
        assertThat(newsRepository.count()).isEqualTo(expectedNewsCount);

        // break transaction on main service
        nestedTransactionsService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSaveAndFlush());

        expectedNewsCount += 3;
        assertThat(newsRepository.count()).isEqualTo(expectedNewsCount);

        // break transaction on sub service
        nestedTransactionsService.setShouldThrowRuntimeException(false);
        nestedTransactionsSubService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSaveAndFlush());

        expectedNewsCount += 1;
        assertThat(newsRepository.count()).isEqualTo(expectedNewsCount);

        // save one more time to be sure
        nestedTransactionsSubService.setShouldThrowRuntimeException(false);

        nestedTransactionsService.transactionWithSaveAndFlush();

        expectedNewsCount += 4;
        assertThat(newsRepository.count()).isEqualTo(expectedNewsCount);
    }

    // https://stackoverflow.com/questions/27987097/disabling-transaction-on-spring-testing-test-method
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldDisableSingleTransaction_forNestedTransactions() {
        long expectedNewsCount = newsRepository.count();

        // save in single transaction
        nestedTransactionsService.transactionWithSaveAndFlush();

        expectedNewsCount += 4;
        assertThat(newsRepository.count()).isEqualTo(expectedNewsCount);

        // break transaction on main service
        nestedTransactionsService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSaveAndFlush());

        assertThat(newsRepository.count()).isEqualTo(expectedNewsCount);

        // break transaction on sub service
        nestedTransactionsService.setShouldThrowRuntimeException(false);
        nestedTransactionsSubService.setShouldThrowRuntimeException(true);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> nestedTransactionsService.transactionWithSaveAndFlush());

        assertThat(newsRepository.count()).isEqualTo(expectedNewsCount);

        // save one more time to be sure
        nestedTransactionsSubService.setShouldThrowRuntimeException(false);

        nestedTransactionsService.transactionWithSaveAndFlush();

        expectedNewsCount += 4;
        assertThat(newsRepository.count()).isEqualTo(expectedNewsCount);
    }

}
