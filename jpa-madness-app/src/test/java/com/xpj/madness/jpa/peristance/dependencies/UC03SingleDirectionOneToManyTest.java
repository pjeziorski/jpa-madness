package com.xpj.madness.jpa.peristance.dependencies;

import com.xpj.madness.jpa.peristance.dependencies.entity.UC03GenericCoupon;
import com.xpj.madness.jpa.peristance.dependencies.entity.UC03User;
import com.xpj.madness.jpa.peristance.dependencies.repository.UC03UserRepository;
import com.xpj.madness.jpa.utils.AdHocTransaction;
import com.xpj.madness.jpa.utils.HibernateStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Transactional(propagation = Propagation.NOT_SUPPORTED) // all tests are not wrapped in single transaction
@Import({AdHocTransaction.class, HibernateStatistics.class})
public class UC03SingleDirectionOneToManyTest {

    @Autowired
    private UC03UserRepository uc03UserRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Autowired
    private HibernateStatistics hibernateStatistics;

    @BeforeEach
    public void beforeEach() {
        System.out.println("Initial query count: " + hibernateStatistics.getQueryCount());
    }

    @Test
    public void shouldSaveUser_withNewGenericCoupons_withoutUserIdAssigned() {
        long initialQueryCount = hibernateStatistics.getQueryCount();

        // given
        UC03User user = UC03User.builder()
                .name("user_1")
                .genericCoupons(List.of(
                        UC03GenericCoupon.builder()
                                .code("gen-code-1")
                                .build(),
                        UC03GenericCoupon.builder()
                                .code("gen-code-2")
                                .build()
                ))
                .build();

        // when
        UC03User savedUser = uc03UserRepository.saveAndFlush(user);

        // then
        // 1 insert of User
        // 2 insert of generic coupon
        // 2 updates of generic coupon
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(initialQueryCount + 5);

        adHocTransaction.readCommitted(() -> {
            UC03User foundUser = uc03UserRepository.findById(savedUser.getId()).get();

            assertThat(foundUser).isNotSameAs(savedUser);

            // savedUser has GenericCoupons with proper id-s but no user_id
            assertThat(savedUser.getGenericCoupons().size()).isEqualTo(user.getGenericCoupons().size());

            savedUser.getGenericCoupons().forEach(genericCoupon -> {
                assertThat(genericCoupon.getId()).isNotNull();
                assertThat(genericCoupon.getUserId()).isNull();
            });

            // however fetched user has GenericCoupons with user_id assigned
            assertThat(foundUser.getGenericCoupons().size()).isEqualTo(user.getGenericCoupons().size());

            foundUser.getGenericCoupons().forEach(genericCoupon -> {
                assertThat(genericCoupon.getId()).isNotNull();
                assertThat(genericCoupon.getUserId()).isEqualTo(savedUser.getId());
            });
        });
    }

    // TODO verify @Test
    public void shouldSaveUser_withNewGenericCoupons_withUserIdAssigned() {
        long initialQueryCount = hibernateStatistics.getQueryCount();

        // given
        String userId = UUID.randomUUID().toString();

        UC03User user = UC03User.builder()
                .id(userId)
                .name("user_1")
                .genericCoupons(List.of(
                        UC03GenericCoupon.builder()
                                .userId(userId)
                                .code("gen-code-1")
                                .build(),
                        UC03GenericCoupon.builder()
                                .userId(userId)
                                .code("gen-code-2")
                                .build()
                ))
                .build();

        // when
        UC03User savedUser = uc03UserRepository.saveAndFlush(user);

        // then
        // 1 insert of User
        // 2 insert of generic coupon
        // 2 updates of generic coupon (even thought it has been set)
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(initialQueryCount + 5);

        adHocTransaction.readCommitted(() -> {
            UC03User foundUser = uc03UserRepository.findById(savedUser.getId()).get();

            assertThat(foundUser).isNotSameAs(savedUser);

            assertThat(savedUser.getGenericCoupons().size()).isEqualTo(user.getGenericCoupons().size());

            savedUser.getGenericCoupons().forEach(genericCoupon -> {
                assertThat(genericCoupon.getId()).isNotNull();
                assertThat(genericCoupon.getUserId()).isNotNull();
            });

            // however fetched user has GenericCoupons with user_id assigned
            assertThat(foundUser.getGenericCoupons().size()).isEqualTo(user.getGenericCoupons().size());

            foundUser.getGenericCoupons().forEach(genericCoupon -> {
                assertThat(genericCoupon.getId()).isNotNull();
                assertThat(genericCoupon.getUserId()).isEqualTo(savedUser.getId());
            });
        });
    }

}
