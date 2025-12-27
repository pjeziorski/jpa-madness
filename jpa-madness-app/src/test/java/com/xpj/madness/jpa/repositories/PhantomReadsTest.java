package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.legacy.entities.OfferProcess;
import com.xpj.madness.jpa.legacy.entities.OfferProcessStatus;
import com.xpj.madness.jpa.legacy.repositories.OfferProcessRepository;
import com.xpj.madness.jpa.legacy.services.ActiveProfileService;
import com.xpj.madness.jpa.legacy.services.IsolationLevelOnReadsService;
import com.xpj.madness.jpa.utils.ControllableOperation;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("legacy")
@ComponentScan("com.xpj.madness.jpa.legacy.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class PhantomReadsTest {

    @Autowired
    private IsolationLevelOnReadsService service;

    @Autowired
    private OfferProcessRepository offerProcessRepository;

    @Autowired
    private ActiveProfileService activeProfileService;

    @BeforeEach
    public void setUp() {
        offerProcessRepository.deleteAll();
    }

    @Test
    public void performTest_onReadCommitted() {
        performTest(Isolation.READ_COMMITTED, 1, 3);
    }

    @Test
    @Disabled // to be checked
    public void performTest_onRepeatableRead() {
        if (activeProfileService.isMssql()) {
            /*
            Expect 3 - update is also visible as it was not read at first place
             */
            performTest(Isolation.REPEATABLE_READ, 1, 3);
        }
        else {
            /*
            Postgres and H2 uses Multi-Version Concurrency Control (MVCC) so Phantom Reads are not possible
             */
            performTest(Isolation.REPEATABLE_READ, 1, 1);
        }
    }

    @Test
    public void performTest_onSerializable() {
        if (activeProfileService.isMssql()) {
            /*
            Mssql blocks updates on this level
             */
            performTest_withSaveAsync(Isolation.SERIALIZABLE, 1, 1);
        }
        else {
            performTest(Isolation.SERIALIZABLE, 1, 1);
        }
    }

    private void performTest(
            Isolation isolationLevel,
            int initialCount,
            int afterSavesCount) {
        offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.OPEN));
        OfferProcess offerToUpdate = offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.CANCELLED));
        offerToUpdate.setStatus(OfferProcessStatus.OPEN);

        Set<OfferProcess> offerProcessList;

        ControllableOperation<OfferProcess> insertOperation = service.insertAndFlush(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<OfferProcess> updateOperation = service.updateAndFlush(isolationLevel, offerToUpdate);
        ControllableOperation<Set<OfferProcess>> findOperation = service.findByStatus(isolationLevel, OfferProcessStatus.OPEN, 2);

        System.err.println("\n=== START ===\n");

        findOperation.start();
        insertOperation.start();
        updateOperation.start();

        // initial
        System.err.println("\nSTART initialCount");
        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("initialCount")
                .isEqualTo(initialCount);

        // save new rows
        System.err.println("\nSTART saveNewRows");
        updateOperation.complete();
        insertOperation.complete();

        // check new count
        System.err.println("\nSTART afterSavesCount");

        offerProcessList = findOperation.complete();
        assertThat(offerProcessList.size()).describedAs("afterSavesCount")
                .isEqualTo(afterSavesCount);
    }

    @SneakyThrows
    private void performTest_withSaveAsync(
            Isolation isolationLevel,
            int initialCount,
            int afterSavesCount) {
        offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.OPEN));
        OfferProcess offerToUpdate = offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.CANCELLED));
        offerToUpdate.setStatus(OfferProcessStatus.OPEN);

        Set<OfferProcess> offerProcessList;

        ControllableOperation<OfferProcess> insertOperation = service.insertAndFlush(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<OfferProcess> updateOperation = service.updateAndFlush(isolationLevel, offerToUpdate);
        ControllableOperation<Set<OfferProcess>> findOperation = service.findByStatus(isolationLevel, OfferProcessStatus.OPEN, 2);

        System.err.println("\n=== START ===\n");

        findOperation.start();
        insertOperation.start();
        updateOperation.start();

        // initial
        System.err.println("\nSTART initialCount");
        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("initialCount")
                .isEqualTo(initialCount);

        // save new rows
        System.err.println("\nSTART saveNewRows");
        Future<OfferProcess> updateFutureResult = updateOperation.completeAsync();
        Future<OfferProcess> insertFutureResult = insertOperation.completeAsync();

        try {
            updateFutureResult.get(500, TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
            // continue
        }

        try {
            insertFutureResult.get(500, TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
            // continue
        }

        // check new count
        System.err.println("\nSTART afterSavesCount");

        offerProcessList = findOperation.complete();
        assertThat(offerProcessList.size()).describedAs("afterSavesCount")
                .isEqualTo(afterSavesCount);

        System.err.println("\nSTART make sure saves finish");
        updateFutureResult.get(5000, TimeUnit.MILLISECONDS);
        insertFutureResult.get(5000, TimeUnit.MILLISECONDS);
    }

    private OfferProcess createOfferProcess(OfferProcessStatus status) {
        return OfferProcess.builder()
                .status(status)
                .creationTime(OffsetDateTime.now())
                .build();
    }
}
