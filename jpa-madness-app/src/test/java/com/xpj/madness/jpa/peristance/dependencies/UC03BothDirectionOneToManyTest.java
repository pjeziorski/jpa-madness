package com.xpj.madness.jpa.peristance.dependencies;

import com.xpj.madness.jpa.peristance.dependencies.entity.UC03User;
import com.xpj.madness.jpa.peristance.dependencies.entity.UC03UserAddress;
import com.xpj.madness.jpa.peristance.dependencies.repository.UC03UserAddressRepository;
import com.xpj.madness.jpa.peristance.dependencies.repository.UC03UserRepository;
import com.xpj.madness.jpa.utils.AdHocTransaction;
import com.xpj.madness.jpa.utils.HibernateStatistics;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Transactional(propagation = Propagation.NOT_SUPPORTED) // all tests are not wrapped in single transaction
@Import({AdHocTransaction.class, HibernateStatistics.class})
public class UC03BothDirectionOneToManyTest {

    @Autowired
    private UC03UserRepository uc03UserRepository;
    
    @Autowired
    private UC03UserAddressRepository uc03UserAddressRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Autowired
    private HibernateStatistics hibernateStatistics;

    @PostConstruct
    public void prepareDatabase() {
        // initialize sequences
        UC03User user = UC03User.builder()
                .name("user_0")
                .addresses(List.of(
                        UC03UserAddress.builder()
                                .city("city_0")
                                .build())
                )
                .build();
        user.getAddresses().forEach(userAddress -> userAddress.setUser(user));

        uc03UserRepository.saveAndFlush(user);
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("Initial query count: " + hibernateStatistics.getQueryCount());
    }

    @Test
    public void shouldSaveUser_withNewAddresses() {
        long initialQueryCount = hibernateStatistics.getQueryCount();

        // given
        UC03User user = UC03User.builder()
                .name("user_1")
                .addresses(List.of(
                        UC03UserAddress.builder()
                                .city("city_1")
                                .build(),
                        UC03UserAddress.builder()
                                .city("city_2")
                                .build()
                ))
                .build();

        // important assignment
        user.getAddresses().forEach(userAddress -> userAddress.setUser(user));

        // when
        UC03User savedUser = uc03UserRepository.saveAndFlush(user);

        // then
        // 1 insert of User + 2 insert of address
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(initialQueryCount + 3);

        assertThat(savedUser.getUserCoupons()).isNull();
        assertThat(savedUser.getGenericCoupons()).isNull();


        adHocTransaction.readCommitted(() -> {
            UC03User foundUser = uc03UserRepository.findById(savedUser.getId()).get();

            assertThat(foundUser.getUserCoupons()).isEmpty();
            assertThat(foundUser.getGenericCoupons()).isEmpty();

            // to compare all fields
            savedUser.setUserCoupons(List.of());
            savedUser.setGenericCoupons(List.of());

            assertThat(foundUser).isNotSameAs(savedUser);
            assertThat(foundUser).usingRecursiveComparison().isEqualTo(savedUser);
        });
    }

    @Test
    public void shouldNotSaveUser_withNewAddresses_whenAddressReferencesAreNotSet() {
        // given
        UC03User user = UC03User.builder()
                .name("user_1")
                .addresses(List.of(
                        UC03UserAddress.builder()
                                .city("city_1")
                                .build(),
                        UC03UserAddress.builder()
                                .city("city_2")
                                .build()
                ))
                .build();

        // when, then
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> uc03UserRepository.saveAndFlush(user));
    }
    
    @Test
    public void shouldAllowSaveAddress_withMinimumUser() {
        // given
        UC03User initUser = UC03User.builder()
                .name("user_3")
                .build();

        initUser = uc03UserRepository.saveAndFlush(initUser);
        
        assertThat(initUser.getId()).isNotNull();

        UC03User minimumUser = UC03User.builder()
                                .id(initUser.getId())
                                .build();

        UC03UserAddress userAddress = UC03UserAddress.builder()
                .city("city_5")
                .user(minimumUser)
                .build();

        // when
        userAddress = uc03UserAddressRepository.saveAndFlush(userAddress);
        
        // then
        UC03User expectedUser = UC03User.builder()
                .id(initUser.getId())
                .name("user_3")
                .addresses(List.of(UC03UserAddress.builder()
                                .id(userAddress.getId())
                                .city("city_5")
                                .build())
                )
                .userCoupons(List.of())
                .genericCoupons(List.of())
                .build();

        expectedUser.getAddresses().forEach(userAddrr -> userAddrr.setUser(expectedUser));

        adHocTransaction.readCommitted(() -> {
            UC03User foundUser = uc03UserRepository.findById(expectedUser.getId()).get();

            assertThat(foundUser).usingRecursiveComparison().isEqualTo(expectedUser);
        });
    }

    @Test
    public void shouldNotAllowSaveAddress_withoutUser() {
        // given
        UC03UserAddress userAddress = UC03UserAddress.builder()
                .city("city_5")
                .build();

        // when, then
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> uc03UserAddressRepository.saveAndFlush(userAddress));
    }

    @Test
    public void shouldSaveUser_withAdditionalAddresses() {
        // given
        UC03User savedUser = adHocTransaction.readCommitted(() -> {
            UC03User user = UC03User.builder()
                    .name("user_1")
                    .addresses(List.of(
                            UC03UserAddress.builder()
                                    .city("city_1")
                                    .build()
                    ))
                    .build();
            user.getAddresses().forEach(userAddress -> userAddress.setUser(user));

            return uc03UserRepository.saveAndFlush(user);
        });

        long initialQueryCount = hibernateStatistics.getQueryCount();
        System.out.println("Before 'when' query count: " + initialQueryCount);

        // when, then
        adHocTransaction.readCommitted(() -> {
            UC03User user = uc03UserRepository.findById(savedUser.getId()).get();
            user.getAddresses().add(UC03UserAddress.builder()
                    .city("city_2")
                    .user(user)
                    .build());

            uc03UserRepository.saveAndFlush(user);

            // 1 select user (don't know why...)
            // 1 insert of address
            assertThat(hibernateStatistics.getQueryCount())
                    .isEqualTo(initialQueryCount + 2);
        });
    }

    @Test
    public void shouldSaveUser_withAdditionalAddresses_outsideTransaction() {
        // given
        UC03User savedUser = adHocTransaction.readCommitted(() -> {
            UC03User user = UC03User.builder()
                    .name("user_1")
                    .addresses(List.of(
                            UC03UserAddress.builder()
                                    .city("city_1")
                                    .build()
                    ))
                    .build();
            user.getAddresses().forEach(userAddress -> userAddress.setUser(user));

            return uc03UserRepository.saveAndFlush(user);
        });

        UC03User foundUser = adHocTransaction.readCommitted(() -> {
            UC03User user = uc03UserRepository.findById(savedUser.getId()).get();
            user.getAddresses().size(); // init lazy loading

            return user;
        });

        foundUser.getAddresses().add(UC03UserAddress.builder()
                        .city("city_2")
                        .user(foundUser)
                        .build());

        long initialQueryCount = hibernateStatistics.getQueryCount();
        System.out.println("Before 'when' query count: " + initialQueryCount);

        // when
        uc03UserRepository.saveAndFlush(foundUser);

        // then
        // 1 select address joined user
        // 1select coupons
        // 1 select generic coupons
        // 1 insert of address
        assertThat(hibernateStatistics.getQueryCount())
                .isEqualTo(initialQueryCount + 4);
    }
}
