package com.xpj.madness.jpa.peristance.references;

import com.xpj.madness.jpa.peristance.references.entity.UC02CityEntity;
import com.xpj.madness.jpa.peristance.references.repository.UC02CityEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class UC02EntityReferencesTest {

    @Autowired
    UC02CityEntityRepository cityEntityRepository;

    /**
     * Default Unit Test single transaction
     */
    @Test
    public void shouldReturn_sameReference_onSingleTransaction() {
        // given
        UC02CityEntity city = UC02CityEntity.builder()
                .name("city 1")
                .build();

        // when
        UC02CityEntity savedCity = cityEntityRepository.save(city);

        // then
        assertThat(savedCity.getId()).isNotNull();
        assertThat(savedCity).isSameAs(city);

        // also
        UC02CityEntity foundCity = cityEntityRepository.findById(savedCity.getId()).get();

        assertThat(foundCity).isSameAs(savedCity);
    }

    /**
     * Default Unit Test single transaction
     */
    @Test
    public void shouldReturn_sameReference_onSingleTransaction_andUsingSaveAndFlush() {
        // given
        UC02CityEntity city = UC02CityEntity.builder()
                .name("city 2")
                .build();

        // when
        UC02CityEntity savedCity = cityEntityRepository.saveAndFlush(city);

        // then
        assertThat(savedCity.getId()).isNotNull();
        assertThat(savedCity).isSameAs(city);

        // also
        UC02CityEntity foundCity = cityEntityRepository.findById(savedCity.getId()).get();

        assertThat(foundCity).isSameAs(savedCity);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldReturn_differentReference_whenCallingFindById_onDifferentTransactions() {
        // given
        UC02CityEntity city = UC02CityEntity.builder()
                .name("city 2")
                .build();

        // when
        UC02CityEntity savedCity = cityEntityRepository.save(city);

        // then
        assertThat(savedCity.getId()).isNotNull();
        assertThat(savedCity).isSameAs(city);

        // also
        UC02CityEntity foundCity = cityEntityRepository.findById(savedCity.getId()).get();

        assertThat(foundCity).isNotSameAs(savedCity);
        assertThat(foundCity).isEqualTo(savedCity);
    }
}
