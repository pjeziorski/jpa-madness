package com.xpj.madness.jpa.peristance.inheritance;

import com.xpj.madness.jpa.peristance.inheritance.entity.UC08JoinedColumnAdamWithLazyChildren;
import com.xpj.madness.jpa.peristance.inheritance.entity.UC08JoinedColumnBethWithEagerChildren;
import com.xpj.madness.jpa.peristance.inheritance.entity.UC08JoinedColumnParent;
import com.xpj.madness.jpa.peristance.inheritance.repository.UC08JoinedColumnParentRepository;
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
public class UC08InheritanceJoinedColumnTest {

    @Autowired
    private UC08JoinedColumnParentRepository UC08JoinedColumnParentRepository;

    @Autowired
    private AdHocTransaction adHocTransaction;

    @Autowired
    private HibernateStatistics hibernateStatistics;

    @PostConstruct
    public void prepareDatabase() {
        // initialize sequences
        UC08JoinedColumnAdamWithLazyChildren entity = UC08JoinedColumnAdamWithLazyChildren.builder()
                .testId("init-sequences")
                .adamSurname("for init sequences")
                .build();
        UC08JoinedColumnParentRepository.saveAndFlush(entity);
    }

    @Test
    public void shouldFind_AdamById() {
        // given
        UC08JoinedColumnAdamWithLazyChildren entity = UC08JoinedColumnTableTestData.createUC08JoinedColumnAdamWithLazyChildren("test01", "findById");

        entity = UC08JoinedColumnParentRepository.saveAndFlush(entity);

        Long entityId = entity.getId();

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            UC08JoinedColumnParent foundEntity = UC08JoinedColumnParentRepository.findById(entityId).get();

            assertThat(foundEntity).isInstanceOf(UC08JoinedColumnAdamWithLazyChildren.class);

            // 1 select
            assertThat(hibernateStatistics.getQueryCount())
                    .isEqualTo(initialQueryCount + 1);
        });
    }

    @Test
    public void shouldFind_BethById() {
        // given
        UC08JoinedColumnBethWithEagerChildren entity = UC08JoinedColumnTableTestData.createUC08JoinedColumnBethWithEagerChildren("test02", "findById");

        entity = UC08JoinedColumnParentRepository.saveAndFlush(entity);

        Long entityId = entity.getId();

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            UC08JoinedColumnParent foundEntity = UC08JoinedColumnParentRepository.findById(entityId).get();

            assertThat(foundEntity).isInstanceOf(UC08JoinedColumnBethWithEagerChildren.class);

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
        UC08JoinedColumnParentRepository.saveAndFlush(UC08JoinedColumnTableTestData.createUC08JoinedColumnAdamWithLazyChildren(testId, "adam1"));
        UC08JoinedColumnParentRepository.saveAndFlush(UC08JoinedColumnTableTestData.createUC08JoinedColumnBethWithEagerChildren(testId, "beth1"));
        UC08JoinedColumnParentRepository.saveAndFlush(UC08JoinedColumnTableTestData.createUC08JoinedColumnAdamWithLazyChildren(testId, "adam2"));
        UC08JoinedColumnParentRepository.saveAndFlush(UC08JoinedColumnTableTestData.createUC08JoinedColumnBethWithEagerChildren(testId, "beth2"));

        // when, then
        adHocTransaction.readCommitted(() -> {
            long initialQueryCount = hibernateStatistics.getQueryCount();
            System.out.println("Before 'when' query count: " + initialQueryCount);

            Collection<UC08JoinedColumnParent> foundEntities = UC08JoinedColumnParentRepository.findAllByTestId(testId);

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
        UC08JoinedColumnAdamWithLazyChildren entity = UC08JoinedColumnAdamWithLazyChildren.builder()
                .testId("init-sequences")
                .build();

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> UC08JoinedColumnParentRepository.save(entity));
    }

}
