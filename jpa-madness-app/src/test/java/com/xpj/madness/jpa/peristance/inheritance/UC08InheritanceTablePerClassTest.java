package com.xpj.madness.jpa.peristance.inheritance;

import com.xpj.madness.jpa.peristance.inheritance.entity.UC08SeparateAdamWithLazyChildren;
import com.xpj.madness.jpa.peristance.inheritance.entity.UC08SeparateBethWithEagerChildren;
import com.xpj.madness.jpa.peristance.inheritance.entity.UC08SeparateParent;
import com.xpj.madness.jpa.peristance.inheritance.repository.UC08SeparateParentRepository;
import com.xpj.madness.jpa.utils.AdHocTransaction;
import com.xpj.madness.jpa.utils.HibernateStatistics;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
@Transactional(propagation = Propagation.NOT_SUPPORTED) // all tests are not wrapped in single transaction
@Import({AdHocTransaction.class, HibernateStatistics.class})
public class UC08InheritanceTablePerClassTest {

    @Autowired
    private UC08SeparateParentRepository uc08SeparateParentRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Autowired
    private HibernateStatistics hibernateStatistics;

    @PostConstruct
    public void prepareDatabase() {
        // initialize sequences
        UC08SeparateAdamWithLazyChildren entity = UC08SeparateAdamWithLazyChildren.builder()
                .testId("init-sequences")
                .adamSurname("for init sequences")
                .build();
        uc08SeparateParentRepository.saveAndFlush(entity);
    }

    @Test
    public void shouldFind_AdamById() {
        // given
        UC08SeparateAdamWithLazyChildren entity = UC08SeparateTableTestData.createUC08SeparateAdamWithLazyChildren("test01", "findById");

        entity = uc08SeparateParentRepository.saveAndFlush(entity);

        Long entityId = entity.getId();

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            UC08SeparateParent foundEntity = uc08SeparateParentRepository.findById(entityId).get();

            assertThat(foundEntity).isInstanceOf(UC08SeparateAdamWithLazyChildren.class);

            // 1 select
            assertThat(hibernateStatistics.getQueryCount())
                    .isEqualTo(initialQueryCount + 1);
        });
    }

    @Test
    public void shouldFind_BethById() {
        // given
        UC08SeparateBethWithEagerChildren entity = UC08SeparateTableTestData.createUC08SeparateBethWithEagerChildren("test02", "findById");

        entity = uc08SeparateParentRepository.saveAndFlush(entity);

        Long entityId = entity.getId();

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            UC08SeparateParent foundEntity = uc08SeparateParentRepository.findById(entityId).get();

            assertThat(foundEntity).isInstanceOf(UC08SeparateBethWithEagerChildren.class);

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
        uc08SeparateParentRepository.saveAndFlush(UC08SeparateTableTestData.createUC08SeparateAdamWithLazyChildren(testId, "adam1"));
        uc08SeparateParentRepository.saveAndFlush(UC08SeparateTableTestData.createUC08SeparateBethWithEagerChildren(testId, "beth1"));
        uc08SeparateParentRepository.saveAndFlush(UC08SeparateTableTestData.createUC08SeparateAdamWithLazyChildren(testId, "adam2"));
        uc08SeparateParentRepository.saveAndFlush(UC08SeparateTableTestData.createUC08SeparateBethWithEagerChildren(testId, "beth2"));

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            Collection<UC08SeparateParent> foundEntities = uc08SeparateParentRepository.findAllByTestId(testId);

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

    @Test
    public void shouldFail_storingNotNullField() {
        // initialize sequences
        UC08SeparateAdamWithLazyChildren entity = UC08SeparateAdamWithLazyChildren.builder()
                .testId("init-sequences")
                .build();

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> uc08SeparateParentRepository.save(entity));
    }


}
