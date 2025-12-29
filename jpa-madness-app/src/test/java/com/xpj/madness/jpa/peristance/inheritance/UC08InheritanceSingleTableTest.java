package com.xpj.madness.jpa.peristance.inheritance;

import com.xpj.madness.jpa.peristance.inheritance.entity.UC08SingleAdamWithLazyChildren;
import com.xpj.madness.jpa.peristance.inheritance.entity.UC08SingleParent;
import com.xpj.madness.jpa.peristance.inheritance.repository.UC08SingleParentRepository;
import com.xpj.madness.jpa.utils.AdHocTransaction;
import com.xpj.madness.jpa.utils.HibernateStatistics;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Transactional(propagation = Propagation.NOT_SUPPORTED) // all tests are not wrapped in single transaction
@Import({AdHocTransaction.class, HibernateStatistics.class})
public class UC08InheritanceSingleTableTest {

    @Autowired
    private UC08SingleParentRepository uc08SingleParentRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Autowired
    private HibernateStatistics hibernateStatistics;

    @PostConstruct
    public void prepareDatabase() {
        // initialize sequences
        UC08SingleAdamWithLazyChildren entity = UC08SingleAdamWithLazyChildren.builder()
                .testId("init-sequences")
                .adamSurname("for init sequences")
                .build();
        uc08SingleParentRepository.saveAndFlush(entity);
    }

    @Test
    public void shouldFindAdamById() {
        // given
        UC08SingleAdamWithLazyChildren entity = UC08SingleTableTestData.createUC08SingleAdamWithLazyChildren("test01", "findById");

        entity = uc08SingleParentRepository.saveAndFlush(entity);

        Long entityId = entity.getId();

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            UC08SingleParent foundEntity = uc08SingleParentRepository.findById(entityId).get();

            assertThat(foundEntity).isInstanceOf(UC08SingleAdamWithLazyChildren.class);

            // 1 select
            assertThat(hibernateStatistics.getQueryCount())
                    .isEqualTo(initialQueryCount + 1);
        });
    }

}
