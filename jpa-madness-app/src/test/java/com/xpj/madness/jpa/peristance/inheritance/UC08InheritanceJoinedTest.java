package com.xpj.madness.jpa.peristance.inheritance;

import com.xpj.madness.jpa.peristance.inheritance.entity.*;
import com.xpj.madness.jpa.peristance.inheritance.repository.UC08JoinedParentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Transactional(propagation = Propagation.NOT_SUPPORTED) // all tests are not wrapped in single transaction
@Import({AdHocTransaction.class, HibernateStatistics.class})
public class UC08InheritanceJoinedTest {

    @Autowired
    private UC08JoinedParentRepository uc08JoinedParentRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Autowired
    private HibernateStatistics hibernateStatistics;

    @PostConstruct
    public void prepareDatabase() {
        // initialize sequences
        UC08JoinedAdamWithLazyChildren entity = UC08JoinedAdamWithLazyChildren.builder()
                .testId("init-sequences")
                .adamSurname("for init sequences")
                .build();
        uc08JoinedParentRepository.saveAndFlush(entity);
    }

    @Test
    public void shouldFind_AdamById() {
        // given
        UC08JoinedAdamWithLazyChildren entity = UC08JoinedTableTestData.createUC08JoinedAdamWithLazyChildren("test01", "findById");

        entity = uc08JoinedParentRepository.saveAndFlush(entity);

        Long entityId = entity.getId();

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            UC08JoinedParent foundEntity = uc08JoinedParentRepository.findById(entityId).get();

            assertThat(foundEntity).isInstanceOf(UC08JoinedAdamWithLazyChildren.class);

            // 1 select
            assertThat(hibernateStatistics.getQueryCount())
                    .isEqualTo(initialQueryCount + 1);
        });
    }

    @Test
    public void shouldFind_BethById() {
        // given
        UC08JoinedBethWithEagerChildren entity = UC08JoinedTableTestData.createUC08JoinedBethWithEagerChildren("test02", "findById");

        entity = uc08JoinedParentRepository.saveAndFlush(entity);

        Long entityId = entity.getId();

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            UC08JoinedParent foundEntity = uc08JoinedParentRepository.findById(entityId).get();

            assertThat(foundEntity).isInstanceOf(UC08JoinedBethWithEagerChildren.class);

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
        uc08JoinedParentRepository.saveAndFlush(UC08JoinedTableTestData.createUC08JoinedAdamWithLazyChildren(testId, "adam1"));
        uc08JoinedParentRepository.saveAndFlush(UC08JoinedTableTestData.createUC08JoinedBethWithEagerChildren(testId, "beth1"));
        uc08JoinedParentRepository.saveAndFlush(UC08JoinedTableTestData.createUC08JoinedAdamWithLazyChildren(testId, "adam2"));
        uc08JoinedParentRepository.saveAndFlush(UC08JoinedTableTestData.createUC08JoinedBethWithEagerChildren(testId, "beth2"));

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            Collection<UC08JoinedParent> foundEntities = uc08JoinedParentRepository.findAllByTestId(testId);

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
