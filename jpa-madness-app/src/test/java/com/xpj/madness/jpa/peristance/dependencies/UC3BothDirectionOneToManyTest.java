package com.xpj.madness.jpa.peristance.dependencies;

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
public class UC3BothDirectionOneToManyTest {

    @Autowired
    private UC3UserRepository uc3UserRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Test
    public void shouldSaveUser_withNewAddresses() {
        // given
        UC3User user = UC3User.builder()
                .name("user_1")
                .addresses(List.of(
                        UC3UserAddress.builder()
                                .city("city_1")
                                .build(),
                        UC3UserAddress.builder()
                                .city("city_2")
                                .build()
                ))
                .build();

        user.getAddresses().forEach(userAddress -> userAddress.setUser(user));

        // when
        UC3User savedUser = uc3UserRepository.saveAndFlush(user);

        // then
        adHocTransaction.readCommitted(() -> {
            UC3User foundUser = uc3UserRepository.findById(savedUser.getId()).get();

            assertThat(foundUser).isNotSameAs(savedUser);
            assertThat(foundUser).usingRecursiveComparison().isEqualTo(savedUser);
        });
    }

    @Test
    public void shouldNotSaveUser_withNewAddresses_whenAddressReferencesAreNotSet() {
        // given
        UC3User user = UC3User.builder()
                .name("user_1")
                .addresses(List.of(
                        UC3UserAddress.builder()
                                .city("city_1")
                                .build(),
                        UC3UserAddress.builder()
                                .city("city_2")
                                .build()
                ))
                .build();

        // when
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> uc3UserRepository.saveAndFlush(user));
    }
}
