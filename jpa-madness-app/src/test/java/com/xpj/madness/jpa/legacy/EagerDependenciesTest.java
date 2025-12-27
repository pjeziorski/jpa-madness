package com.xpj.madness.jpa.legacy;

import com.xpj.madness.jpa.legacy.entities.Basket;
import com.xpj.madness.jpa.legacy.entities.BasketItem;
import com.xpj.madness.jpa.legacy.repositories.BasketRepository;
import com.xpj.madness.jpa.utils.HibernateStatistics;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Deprecated
@DataJpaTest
@ActiveProfiles("legacy")
@ComponentScan("com.xpj.madness.jpa.legacy.services")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Import(HibernateStatistics.class)
public class EagerDependenciesTest {

    @Autowired
    BasketRepository basketRepository;

    @Autowired
    HibernateStatistics hibernateStatistics;

    @BeforeEach
    public void beforeEach() {
        System.out.println("Initial query count: " + hibernateStatistics.getQueryCount());
    }

    @Test
    public void shouldMakeQuery_afterFlush() {
        long expectedQueryCount = hibernateStatistics.getQueryCount();

        Basket basket1 = Basket.builder().build();

        basketRepository.save(basket1);

        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);

        basketRepository.flush();

        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
    public void shouldMake_properQueryCalls() {
        long expectedQueryCount = hibernateStatistics.getQueryCount();

        Basket basket = Basket.builder()
                .items(List.of(
                        BasketItem.builder().build(),
                        BasketItem.builder().build(),
                        BasketItem.builder().build()
                ))
                .build();

        basketRepository.saveAndFlush(basket);

        // 7 calls
        // insert basket
        // 3x insert basket_item
        // 3x update basket_item with basket_id
        expectedQueryCount += 7;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);

        Basket returnedBasket = basketRepository.findById(basket.getId()).get();

        // one new query for basket
        expectedQueryCount += 1;
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);

        // make operation on items
        System.out.println(returnedBasket.getItems().stream()
                .map(BasketItem::toString)
                .collect(Collectors.joining("\n")));

        // no new queries
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(expectedQueryCount);
    }

}
