package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.BasketWithGraph;
import com.xpj.madness.jpa.entities.BasketWithGraphItem;
import com.xpj.madness.jpa.services.BasketWithGraphService;
import com.xpj.madness.jpa.services.HibernateStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
public class LazyDependenciesTest {

    @Autowired
    BasketWithGraphService service;

    @Autowired
    HibernateStatistics hibernateStatistics;

    @BeforeEach
    public void beforeEach() {
        System.out.println("Initial query count: " + hibernateStatistics.getQueryCount());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
    public void shouldNotFetchItems_onInitialRequest() {
        BasketWithGraph basket = BasketWithGraph.builder()
                .title("basket title")
                .items(List.of(
                        BasketWithGraphItem.builder().build(),
                        BasketWithGraphItem.builder().build(),
                        BasketWithGraphItem.builder().build()
                ))
                .build();

        service.saveAndFlush(basket);

        long expectedQueryCount = hibernateStatistics.getQueryCount();

        BasketWithGraph returnedBasket = service.findById(basket.getId()).get();

        // make some operation on title
        System.out.println(returnedBasket.getTitle());

        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);

        // print each item
        service.findByIdAndPrintItems(basket.getId());

        expectedQueryCount += 2;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);
    }
}
