package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.NumberedOfferProcess;
import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.services.ActiveProfileService;
import com.xpj.madness.jpa.services.IsolationLevelOnReadsNumberedService;
import com.xpj.madness.jpa.utils.ControllableOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class IsolationLevelOnReadsNumbered_H2Test {

    @Autowired
    private IsolationLevelOnReadsNumberedService service;

    @Autowired
    private NumberedOfferProcessRepository numberedOfferProcessRepository;

    @Autowired
    private ActiveProfileService activeProfileService;

    public boolean isActiveProfileH2() {
        return activeProfileService.isDefault();
    }

    @BeforeEach
    public void setUp() {
        numberedOfferProcessRepository.deleteAll();
    }

    @Test
    @EnabledIf("isActiveProfileH2")
    public void performTest_onReadCommitted() {
        performTest(Isolation.READ_COMMITTED, 1, 1, 1,2, 3);
    }

    @Test
    @EnabledIf("isActiveProfileH2")
    public void performTest_onRepeatableRead() {
        performTest(Isolation.REPEATABLE_READ, 1, 1, 1,1, 1);
    }

    @Test
    @EnabledIf("isActiveProfileH2")
    public void performTest_onSerializable() {
        performTest(Isolation.SERIALIZABLE, 1, 1, 1,1, 1);
    }

    private void performTest(
            Isolation isolationLevel,
            int initialCount,
            int afterInitUpdateCount,
            int afterInitInsertCount,
            int afterCompleteUpdateCount,
            int afterCompleteInsertCount) {
        numberedOfferProcessRepository.saveAndFlush(createNumberedOfferProcess(OfferProcessStatus.OPEN));
        NumberedOfferProcess offerToUpdate = numberedOfferProcessRepository.saveAndFlush(createNumberedOfferProcess(OfferProcessStatus.CANCELLED));
        offerToUpdate.setStatus(OfferProcessStatus.OPEN);

        Set<NumberedOfferProcess> offerProcessList;

        ControllableOperation<NumberedOfferProcess> insertOperation = service.insertAndFlush(isolationLevel, createNumberedOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<NumberedOfferProcess> updateOperation = service.updateAndFlush(isolationLevel, offerToUpdate);
        ControllableOperation<Set<NumberedOfferProcess>> findOperation = service.findByStatus(isolationLevel, OfferProcessStatus.OPEN, 5);

        System.err.println("\n=== START ===\n");

        insertOperation.start();
        updateOperation.start();
        findOperation.start();

        // initial
        System.err.println("\nSTART initialCount");
        offerProcessList = (Set<NumberedOfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("initialCount")
                .isEqualTo(initialCount);

        // init update
        System.err.println("\nSTART afterInitUpdateCount");
        updateOperation.resume();

        offerProcessList = (Set<NumberedOfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterInitUpdateCount")
                .isEqualTo(afterInitUpdateCount);

        // init insert
        System.err.println("\nSTART afterInitInsertCount");
        insertOperation.resume();

        offerProcessList = (Set<NumberedOfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterInitInsertCount")
                .isEqualTo(afterInitInsertCount);

        // complete update
        System.err.println("\nSTART afterCompleteUpdateCount");
        updateOperation.complete();

        offerProcessList = (Set<NumberedOfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterCompleteUpdateCount")
                .isEqualTo(afterCompleteUpdateCount);

        // complete insert
        System.err.println("\nSTART afterCompleteInsertCount");
        insertOperation.complete();

        offerProcessList = (Set<NumberedOfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterCompleteInsertCount")
                .isEqualTo(afterCompleteInsertCount);
    }

    @Test
    //@EnabledIf("isActiveProfileH2")
    public void performTestWithInsertOnly_onRepeatableRead() {
        performTestWithInsertOnly(Isolation.REPEATABLE_READ, 1, 1, 1);
    }

    private void performTestWithInsertOnly(
            Isolation isolationLevel,
            int initialCount,
            int afterInitInsertCount,
            int afterCompleteInsertCount) {
        numberedOfferProcessRepository.saveAndFlush(createNumberedOfferProcess(OfferProcessStatus.OPEN));
        numberedOfferProcessRepository.saveAndFlush(createNumberedOfferProcess(OfferProcessStatus.CANCELLED));

        Set<NumberedOfferProcess> offerProcessList;

        ControllableOperation<NumberedOfferProcess> insertOperation = service.insertAndFlush(isolationLevel, createNumberedOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<Set<NumberedOfferProcess>> findOperation = service.findByStatus(isolationLevel, OfferProcessStatus.OPEN, 5);

        System.err.println("\n=== START ===\n");

        findOperation.start();
        insertOperation.start();

        // initial
        System.err.println("\nSTART initialCount");
        offerProcessList = (Set<NumberedOfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("initialCount")
                .isEqualTo(initialCount);

        numberedOfferProcessRepository.saveAndFlush(createNumberedOfferProcess(OfferProcessStatus.OPEN));
        numberedOfferProcessRepository.saveAndFlush(createNumberedOfferProcess(OfferProcessStatus.OPEN));

        // complete insert
        System.err.println("\nSTART afterCompleteInsertCount");
        insertOperation.complete();

        offerProcessList = (Set<NumberedOfferProcess>)findOperation.complete();
        assertThat(offerProcessList.size()).describedAs("afterCompleteInsertCount")
                .isEqualTo(afterCompleteInsertCount);
    }

    private NumberedOfferProcess createNumberedOfferProcess(OfferProcessStatus status) {
        return NumberedOfferProcess.builder()
                .status(status)
                .creationTime(OffsetDateTime.now())
                .build();
    }
}
