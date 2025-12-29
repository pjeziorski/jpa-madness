package com.xpj.madness.jpa.peristance.dependencies;

import com.xpj.madness.jpa.peristance.dependencies.entity.UC06Basket;
import com.xpj.madness.jpa.peristance.dependencies.entity.UC06BasketCoupon;
import com.xpj.madness.jpa.peristance.dependencies.entity.UC06BasketItem;
import com.xpj.madness.jpa.peristance.dependencies.repository.UC06BasketRepository;
import com.xpj.madness.jpa.utils.AdHocTransaction;
import com.xpj.madness.jpa.utils.HibernateStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Transactional(propagation = Propagation.NOT_SUPPORTED) // all tests are not wrapped in single transaction
@Import({AdHocTransaction.class, HibernateStatistics.class})
public class UC06EagerDependenciesTest {

    @Autowired
    private UC06BasketRepository uc06BasketRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Autowired
    private HibernateStatistics hibernateStatistics;

    @Test
    public void shouldAllowUsingCollections_outsideOfTransaction() {
        // given
        String basketId = adHocTransaction.readCommitted(() -> {
            UC06Basket basket = UC06Basket.builder()
                    .userId("user1")
                    .items(List.of(
                            UC06BasketItem.builder()
                                    .name("item_1")
                                    .build(),
                            UC06BasketItem.builder()
                                    .name("item_2")
                                    .build()
                    ))
                    .coupons(List.of(
                            UC06BasketCoupon.builder()
                                    .code("code_1")
                                    .build()
                    ))
                    .build();
            basket.getItems().forEach(basketItem -> basketItem.setBasket(basket));
            basket.getCoupons().forEach(basketCoupon -> basketCoupon.setBasket(basket));

            return uc06BasketRepository.save(basket).getId();
        });
        long initialQueryCount = hibernateStatistics.getQueryCount();
        System.out.println("Before 'when' query count: " + initialQueryCount);

        // when
        UC06Basket foundBasket = adHocTransaction.readCommitted(
                () -> uc06BasketRepository.findById(basketId).get());

        // then
        // 1 query for one eager
        // 1 query for additional eager
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(initialQueryCount + 2);

        assertThat(foundBasket.getItems()).hasSize(2);
        assertThat(foundBasket.getCoupons()).hasSize(1);
    }
}
