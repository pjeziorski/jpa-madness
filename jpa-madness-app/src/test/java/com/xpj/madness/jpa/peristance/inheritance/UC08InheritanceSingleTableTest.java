package com.xpj.madness.jpa.peristance.inheritance;

import com.xpj.madness.jpa.peristance.inheritance.entity.UC08SingleAdamWithLazyChildren;
import com.xpj.madness.jpa.peristance.inheritance.entity.UC08SingleBethWithEagerChildren;
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

import java.util.Collection;
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
    public void shouldFind_AdamById() {
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

    @Test
    public void shouldFind_BethById() {
        // given
        UC08SingleBethWithEagerChildren entity = UC08SingleTableTestData.createUC08SingleBethWithEagerChildren("test02", "findById");

        entity = uc08SingleParentRepository.saveAndFlush(entity);

        Long entityId = entity.getId();

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            UC08SingleParent foundEntity = uc08SingleParentRepository.findById(entityId).get();

            assertThat(foundEntity).isInstanceOf(UC08SingleBethWithEagerChildren.class);

            // 1 select beth and its children
            // 2 select for beth sub children
            assertThat(hibernateStatistics.getQueryCount())
                    .isEqualTo(initialQueryCount + 3);
        });
    }

    @Test
    public void shouldFindAll_AdamAndBeth() {
        // given
        String testId = "test03";
        uc08SingleParentRepository.saveAndFlush(UC08SingleTableTestData.createUC08SingleAdamWithLazyChildren(testId, "adam1"));
        uc08SingleParentRepository.saveAndFlush(UC08SingleTableTestData.createUC08SingleBethWithEagerChildren(testId, "beth1"));
        uc08SingleParentRepository.saveAndFlush(UC08SingleTableTestData.createUC08SingleAdamWithLazyChildren(testId, "adam2"));
        uc08SingleParentRepository.saveAndFlush(UC08SingleTableTestData.createUC08SingleBethWithEagerChildren(testId, "beth2"));

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            Collection<UC08SingleParent> foundEntities = uc08SingleParentRepository.findAllByTestId(testId);

            assertThat(foundEntities).hasSize(4);

            // 1 select beth and its children
            // 1 select for children of beth1
            // 1 select for children of beth2
            // 2 select for sub children of beth1
            // 2 select for sub children of beth2
            assertThat(hibernateStatistics.getQueryCount())
                    .isEqualTo(initialQueryCount + 7);
        });
    }

}
