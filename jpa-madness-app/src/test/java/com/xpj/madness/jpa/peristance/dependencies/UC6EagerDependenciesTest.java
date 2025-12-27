package com.xpj.madness.jpa.peristance.dependencies;

import com.xpj.madness.jpa.entities.Basket;
import com.xpj.madness.jpa.peristance.dependencies.entity.UC6Basket;
import com.xpj.madness.jpa.peristance.dependencies.entity.UC6BasketCoupon;
import com.xpj.madness.jpa.peristance.dependencies.entity.UC6BasketItem;
import com.xpj.madness.jpa.peristance.dependencies.repository.UC6BasketRepository;
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
public class UC6EagerDependenciesTest {

    @Autowired
    private UC6BasketRepository uc6BasketRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Autowired
    private HibernateStatistics hibernateStatistics;

    @Test
    public void shouldAllowUsingCollections_outsideOfTransaction() {
        // given
        String basketId = adHocTransaction.readCommitted(() -> {
            UC6Basket basket = UC6Basket.builder()
                    .userId("user1")
                    .items(List.of(
                            UC6BasketItem.builder()
                                    .name("item_1")
                                    .build(),
                            UC6BasketItem.builder()
                                    .name("item_2")
                                    .build()
                    ))
                    .coupons(List.of(
                            UC6BasketCoupon.builder()
                                    .code("code_1")
                                    .build()
                    ))
                    .build();
            basket.getItems().forEach(basketItem -> basketItem.setBasket(basket));
            basket.getCoupons().forEach(basketCoupon -> basketCoupon.setBasket(basket));

            return uc6BasketRepository.save(basket).getId();
        });
        long initialQueryCount = hibernateStatistics.getQueryCount();
        System.out.println("Before 'when' query count: " + initialQueryCount);

        // when
        UC6Basket foundBasket = adHocTransaction.readCommitted(
                () -> uc6BasketRepository.findById(basketId).get());

        // then
        // 1 query for one eager
        // 1 query for additional eager
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(initialQueryCount + 2);

        assertThat(foundBasket.getItems()).hasSize(2);
        assertThat(foundBasket.getCoupons()).hasSize(1);
    }
}
