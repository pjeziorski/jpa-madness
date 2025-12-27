package com.xpj.madness.jpa.peristance.dependencies;

import com.xpj.madness.jpa.peristance.dependencies.entity.UC3GenericCoupon;
import com.xpj.madness.jpa.peristance.dependencies.entity.UC3User;
import com.xpj.madness.jpa.peristance.dependencies.entity.UC3UserAddress;
import com.xpj.madness.jpa.peristance.dependencies.repository.UC3UserRepository;
import com.xpj.madness.jpa.utils.AdHocTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Transactional(propagation = Propagation.NOT_SUPPORTED) // all tests are not wrapped in single transaction
@Import(AdHocTransaction.class)
public class UC3SingleDirectionOneToManyTest {

    @Autowired
    private UC3UserRepository uc3UserRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Test
    public void shouldSaveUser_withNewGenericCoupons_withoutUserAssigned() {
        // given
        UC3User user = UC3User.builder()
                .name("user_1")
                .genericCoupons(List.of(
                        UC3GenericCoupon.builder()
                                .code("gen-code-1")
                                .build(),
                        UC3GenericCoupon.builder()
                                .code("gen-code-2")
                                .build()
                ))
                .build();

        // when
        UC3User savedUser = uc3UserRepository.saveAndFlush(user);

        // then
        adHocTransaction.readCommitted(() -> {
            UC3User foundUser = uc3UserRepository.findById(savedUser.getId()).get();

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

}
