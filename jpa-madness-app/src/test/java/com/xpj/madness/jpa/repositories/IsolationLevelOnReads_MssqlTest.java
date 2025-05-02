package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.services.ActiveProfileService;
import com.xpj.madness.jpa.services.IsolationLevelOnReadsService;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class IsolationLevelOnReads_MssqlTest {

    @Autowired
    private IsolationLevelOnReadsService service;

    @Autowired
    private OfferProcessRepository offerProcessRepository;

    @Autowired
    private ActiveProfileService activeProfileService;

    public boolean isActiveProfileMssql() {
        return activeProfileService.isMssql();
    }

    @BeforeEach
    public void setUp() {
        offerProcessRepository.deleteAll();
    }

    @Test
    @EnabledIf("isActiveProfileMssql")
    public void performTest_onReadCommitted() throws Exception {
        Isolation isolationLevel = Isolation.READ_COMMITTED;

        offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.OPEN));
        OfferProcess offerToUpdate = offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.CANCELLED));
        offerToUpdate.setStatus(OfferProcessStatus.OPEN);

        ControllableOperation<OfferProcess> insertOperation = service.insertAndFlush(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<OfferProcess> updateOperation = service.updateAndFlush(isolationLevel, offerToUpdate);
        ControllableOperation<Set<OfferProcess>> findOperation = service.findByStatus(isolationLevel, OfferProcessStatus.OPEN, 3);

        Set<OfferProcess> offerProcessList;

        System.err.println("\n=== START ===\n");

        findOperation.start();
        insertOperation.start();
        updateOperation.start();

        // initial
        System.err.println("\nSTART initialCount");
        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("initialCount")
                .isEqualTo(1);

        // init update
        System.err.println("\nSTART afterInitUpdateCount");
        updateOperation.resume();

        Future<?> findOperationResult = findOperation.resumeAsync();

        assertThat(findOperationResult.isDone())
                .describedAs("findOperationResult afterInitUpdateCount")
                .isFalse();

        // complete save operations
        System.err.println("\nSTART afterCompleteSaveOperations");
        insertOperation.complete();
        updateOperation.complete();

        offerProcessList = (Set<OfferProcess>)findOperationResult.get(500, TimeUnit.MILLISECONDS);
        System.err.println("afterCompleteSaveOperations findResultCount: " + offerProcessList.size());
        assertThat(offerProcessList.size()).describedAs("afterCompleteSaveOperations")
                .isBetween(2, 3); // BECAUSE sometime MSSQL decides to unblock 'select' between operations

        // complete find operation
        offerProcessList = findOperation.complete();
        assertThat(offerProcessList.size()).describedAs("afterCompleteFindOperations")
                .isEqualTo(3);
    }

    @Test
    @EnabledIf("isActiveProfileMssql")
    public void performTest_onRepeatableRead() throws Exception {
        Isolation isolationLevel = Isolation.REPEATABLE_READ;

        offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.OPEN));
        OfferProcess offerToUpdate = offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.CANCELLED));
        offerToUpdate.setStatus(OfferProcessStatus.OPEN);

        ControllableOperation<OfferProcess> insertOperation = service.insertAndFlush(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<OfferProcess> updateOperation = service.updateAndFlush(isolationLevel, offerToUpdate);
        ControllableOperation<Set<OfferProcess>> findOperation = service.findByStatus(isolationLevel, OfferProcessStatus.OPEN, 3);

        Set<OfferProcess> offerProcessList;

        System.err.println("\n=== START ===\n");

        findOperation.start();
        insertOperation.start();
        updateOperation.start();

        // initial
        System.err.println("\nSTART initialCount");
        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("initialCount")
                .isEqualTo(1);

        // init update
        System.err.println("\nSTART afterInitUpdateCount");
        updateOperation.resume();

        Future<?> findOperationResult = findOperation.resumeAsync();

        assertThat(findOperationResult.isDone())
                .describedAs("findOperationResult afterInitUpdateCount")
                .isFalse();

        // complete save operations
        System.err.println("\nSTART afterCompleteSaveOperations");
        insertOperation.complete();
        updateOperation.complete();

        offerProcessList = (Set<OfferProcess>)findOperationResult.get(500, TimeUnit.MILLISECONDS);
        System.err.println("afterCompleteSaveOperations findResultCount: " + offerProcessList.size());
        assertThat(offerProcessList.size()).describedAs("afterCompleteSaveOperations")
                .isBetween(2, 3); // BECAUSE sometime MSSQL decides to unblock 'select' between operations

        // complete find operation
        offerProcessList = findOperation.complete();
        assertThat(offerProcessList.size()).describedAs("afterCompleteFindOperations")
                .isEqualTo(3); // LOL should be 2
    }

    @Test
    @EnabledIf("isActiveProfileMssql")
    public void performTest_onSerializable() throws Exception {
        Isolation isolationLevel = Isolation.SERIALIZABLE;

        offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.OPEN));
        OfferProcess offerToUpdate = offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.CANCELLED));
        offerToUpdate.setStatus(OfferProcessStatus.OPEN);

        ControllableOperation<OfferProcess> insertOperation = service.insertAndFlush(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<OfferProcess> updateOperation = service.updateAndFlush(isolationLevel, offerToUpdate);
        ControllableOperation<Set<OfferProcess>> findOperation = service.findByStatus(isolationLevel, OfferProcessStatus.OPEN, 3);

        Set<OfferProcess> offerProcessList;

        System.err.println("\n=== START ===\n");

        findOperation.start();
        insertOperation.start();
        updateOperation.start();

        // initial
        System.err.println("\nSTART initialCount");
        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("initialCount")
                .isEqualTo(1);

        // init update
        System.err.println("\nSTART afterInitUpdateCount");
        updateOperation.resumeAsync();

        Future<?> findOperationResult = findOperation.resumeAsync();

        assertThat(findOperationResult.isDone())
                .describedAs("findOperationResult afterInitUpdateCount")
                .isFalse();

        // complete save operations
        System.err.println("\nSTART afterCompleteSaveOperations");
        insertOperation.complete();
        updateOperation.complete();

        offerProcessList = (Set<OfferProcess>)findOperationResult.get(500, TimeUnit.MILLISECONDS);
        System.err.println("afterCompleteSaveOperations findResultCount: " + offerProcessList.size());
        assertThat(offerProcessList.size()).describedAs("afterCompleteSaveOperations")
                .isBetween(2, 3); // BECAUSE sometime MSSQL decides to unblock 'select' between operations

        // complete find operation
        offerProcessList = findOperation.complete();
        assertThat(offerProcessList.size()).describedAs("afterCompleteFindOperations")
                .isEqualTo(3); // LOL should be 2
    }

    @Test
    @EnabledIf("isActiveProfileMssql")
    public void performTest_onSerializable2() {
        performTest(Isolation.SERIALIZABLE, 1, 1, 1,1, 1);
    }

    private void performTest(
            Isolation isolationLevel,
            int initialCount,
            int afterInitUpdateCount,
            int afterInitInsertCount,
            int afterCompleteUpdateCount,
            int afterCompleteInsertCount) {
        OfferProcess offerProcess1 = service.insertAndFlush(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN))
                .start().complete();
        OfferProcess offerProcess2 = service.insertAndFlush(isolationLevel, createOfferProcess(OfferProcessStatus.CANCELLED))
                .start().complete();
        offerProcess2.setStatus(OfferProcessStatus.OPEN);

        Set<OfferProcess> offerProcessList;

        ControllableOperation<OfferProcess> insertOperation = service.insertAndFlush(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<OfferProcess> updateOperation = service.updateAndFlush(isolationLevel, offerProcess2);
        ControllableOperation<Set<OfferProcess>> findOperation = service.findByStatus(isolationLevel, OfferProcessStatus.OPEN, 5);

        System.err.println("\n=== START ===\n");

        insertOperation.start();
        updateOperation.start();
        findOperation.start();

        // initial
        System.err.println("\nSTART initialCount");
        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("initialCount")
                .isEqualTo(initialCount);

        // init update
        System.err.println("\nSTART afterInitUpdateCount");
        updateOperation.resume();

        findOperation.resumeAsync();
//        offerProcessList = (Set<OfferProcess>)findOperation.resume();
//        assertThat(offerProcessList.size()).describedAs("afterInitUpdateCount")
//                .isEqualTo(afterInitUpdateCount);

        // init insert
        System.err.println("\nSTART afterInitInsertCount");
        insertOperation.resume();

        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterInitInsertCount")
                .isEqualTo(afterInitInsertCount);

        // complete update
        System.err.println("\nSTART afterCompleteUpdateCount");
        updateOperation.complete();

        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterCompleteUpdateCount")
                .isEqualTo(afterCompleteUpdateCount);

        // complete insert
        System.err.println("\nSTART afterCompleteInsertCount");
        insertOperation.complete();

        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterCompleteInsertCount")
                .isEqualTo(afterCompleteInsertCount);
    }

    private OfferProcess createOfferProcess(OfferProcessStatus status) {
        return OfferProcess.builder()
                .status(status)
                .creationTime(OffsetDateTime.now())
                .build();
    }
}
