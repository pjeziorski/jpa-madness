package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.Basket;
import com.xpj.madness.jpa.services.BasketsService;
import com.xpj.madness.jpa.services.HibernateStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
public class UnitTestsTransactionsTest {

    @Autowired
    BasketRepository basketRepository;

    @Autowired
    BasketsService basketsService;

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

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldUseSeparateTransactions() {
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

}
