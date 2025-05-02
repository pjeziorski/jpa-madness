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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class IsolationLevelOnReads_PostgresTest {

    @Autowired
    private IsolationLevelOnReadsService service;

    @Autowired
    private OfferProcessRepository offerProcessRepository;

    @Autowired
    private ActiveProfileService activeProfileService;

    public boolean isActiveProfilePostgres() {
        return activeProfileService.isPostgres();
    }

    @BeforeEach
    public void setUp() {
        offerProcessRepository.deleteAll();
    }

    @Test
    @EnabledIf("isActiveProfilePostgres")
    public void performTest_onReadCommitted() {
        performTest(Isolation.READ_COMMITTED, 1, 1, 1,2, 3);
    }

    @Test
    @EnabledIf("isActiveProfilePostgres")
    public void performTest_onRepeatableRead() {
        performTest(Isolation.REPEATABLE_READ, 1, 1, 1,1, 1);
    }

    @Test
    @EnabledIf("isActiveProfilePostgres")
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
        offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.OPEN));
        OfferProcess offerToUpdate = offerProcessRepository.saveAndFlush(createOfferProcess(OfferProcessStatus.CANCELLED));
        offerToUpdate.setStatus(OfferProcessStatus.OPEN);

        Set<OfferProcess> offerProcessList;

        ControllableOperation<OfferProcess> insertOperation = service.insertAndFlush(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<OfferProcess> updateOperation = service.updateAndFlush(isolationLevel, offerToUpdate);
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

        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterInitUpdateCount")
                .isEqualTo(afterInitUpdateCount);

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
