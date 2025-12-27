package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.BasketWithGraph;
import com.xpj.madness.jpa.entities.BasketWithGraphItem;
import com.xpj.madness.jpa.services.BasketWithGraphService;
import com.xpj.madness.jpa.utils.HibernateStatistics;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@ActiveProfiles("legacy")
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Import(HibernateStatistics.class)
public class LazyDependenciesTest {

    // using service to simulate different transactions
    @Autowired
    BasketWithGraphService service;

    @Autowired
    HibernateStatistics hibernateStatistics;

    @BeforeEach
    public void beforeEach() {
        System.out.println("Initial query count: " + hibernateStatistics.getQueryCount());
    }

    @Test
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

    @Test
    public void shouldFetchItemsBy_findByIdWithEntityGraph() {
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

        BasketWithGraph returnedBasket = service.findByIdWithEntityGraph(basket.getId()).get();

        System.out.println(returnedBasket.getTitle());

        returnedBasket.getItems().stream()
                .forEach(System.out::println);

        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);
    }

    @Test
    public void shouldNotFetchItemsBy_findWithQuery() {
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

        BasketWithGraph returnedBasket = service.findWithQuery(basket.getId()).get();

        System.out.println(returnedBasket.getTitle());

        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);

        // expect new queries
        assertThatExceptionOfType(LazyInitializationException.class).isThrownBy(
                () -> returnedBasket.getItems().stream()
                        .forEach(System.out::println)
        );

        // get items withTransaction
        service.withTransaction(() -> {
            long expQueryCount = hibernateStatistics.getQueryCount();

            BasketWithGraph retBasket = service.findWithQuery(basket.getId()).get();

            retBasket.getItems().stream()
                    .forEach(System.out::println);

            expQueryCount += 2;
            assertThat(hibernateStatistics.getQueryCount())
                    .isEqualTo(expQueryCount);
        });
    }

    @Test
    public void shouldFetchItemsBy_findWithQueryAndEntityGraph() {
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

        BasketWithGraph returnedBasket = service.findWithQueryAndEntityGraph(basket.getId()).get();

        System.out.println(returnedBasket.getTitle());

        returnedBasket.getItems().stream()
                .forEach(System.out::println);

        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);
    }

    @Test
    public void shouldFetchItemsBy_findWithQueryAndJoinFetch() {
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

        BasketWithGraph returnedBasket = service.findWithQueryAndJoinFetch(basket.getId()).get();

        System.out.println(returnedBasket.getTitle());

        returnedBasket.getItems().stream()
                .forEach(System.out::println);

        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);
    }

}
