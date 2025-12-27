package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.utils.ControllableOperation;
import com.xpj.madness.jpa.services.IsolationLevelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("legacy")
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class IsolationLevelTest {

    @Autowired
    private IsolationLevelService isolationLevelService;

    @Autowired
    private OfferProcessRepository offerProcessRepository;

    @BeforeEach
    public void setUp() {
        offerProcessRepository.deleteAll();
    }

    @Test
    public void shouldGetUncommitted_onReadUncommitted() {
        Set<OfferProcess> offerProcessList;

        ControllableOperation<OfferProcess> saveOperation = isolationLevelService.insertAndFlushOfferProcess(OfferProcessStatus.OPEN);
        ControllableOperation<Set<OfferProcess>> findAllOperation = isolationLevelService.findByStatus_onReadUncommitted(OfferProcessStatus.OPEN, 2);

        saveOperation.start();
        findAllOperation.start();

        // save without commit
        saveOperation.resume();

        offerProcessList = (Set<OfferProcess>)findAllOperation.resume();
        assertThat(offerProcessList.size()).isEqualTo(1);

        // do save commit
        saveOperation.complete();

        offerProcessList = (Set<OfferProcess>)findAllOperation.resume();
        assertThat(offerProcessList.size()).isEqualTo(1);
    }

    @Test
    public void shouldNotGetUncommitted_onReadCommitted() {
        Set<OfferProcess> offerProcessList;

        ControllableOperation<OfferProcess> saveOperation = isolationLevelService.insertAndFlushOfferProcess(OfferProcessStatus.OPEN);
        ControllableOperation<Set<OfferProcess>> findAllOperation = isolationLevelService.findByStatus_onReadCommitted(OfferProcessStatus.OPEN, 2);

        saveOperation.start();
        findAllOperation.start();

        // save without commit
        saveOperation.resume();

        offerProcessList = (Set<OfferProcess>)findAllOperation.resume();
        assertThat(offerProcessList.size()).isEqualTo(0);

        // do save commit
        saveOperation.complete();

        offerProcessList = (Set<OfferProcess>)findAllOperation.resume();
        assertThat(offerProcessList.size()).isEqualTo(1);
    }

    @Test
    public void performTest_onReadUncommitted() {
        performTest(
                (readTimes) -> isolationLevelService.findByStatus_onReadUncommitted(OfferProcessStatus.OPEN, readTimes),
                1, 2, 3, 3, 3);
    }
    @Test
    public void performTest_onReadCommitted() {
        performTest(
                (readTimes) -> isolationLevelService.findByStatus_onReadCommitted(OfferProcessStatus.OPEN, readTimes),
                1, 1, 1, 2, 3);
    }

    @Test
    public void performTest_onRepeatableRead() {
        performTest(
                (readTimes) -> isolationLevelService.findByStatus_onRepeatableRead(OfferProcessStatus.OPEN, readTimes),
                1, 1, 1, 1, 1);
    }

    @Test
    public void performTest_onSerializable() {
        performTest(
                (readTimes) -> isolationLevelService.findByStatus(Isolation.SERIALIZABLE, OfferProcessStatus.OPEN, readTimes),
                1, 1, 1, 1, 1);
    }

    private void performTest(
            Function<Integer, ControllableOperation<Set<OfferProcess>>> findOperationTimes,
            int initialCount,
            int afterInitUpdateCount,
            int afterInitInsertCount,
            int afterCompleteUpdateCount,
            int afterCompleteInsertCount) {
        OfferProcess offerProcess1 = isolationLevelService.insertAndFlushOfferProcess(OfferProcessStatus.OPEN).start().complete();
        OfferProcess offerProcess2 = isolationLevelService.insertAndFlushOfferProcess(OfferProcessStatus.CLOSED).start().complete();
        offerProcess2.setStatus(OfferProcessStatus.OPEN);

        Set<OfferProcess> offerProcessList;

        ControllableOperation<OfferProcess> insertOperation = isolationLevelService.insertAndFlushOfferProcess(OfferProcessStatus.OPEN);
        ControllableOperation<OfferProcess> updateOperation = isolationLevelService.updateAndFlushOfferProcess(offerProcess2);
        ControllableOperation<Set<OfferProcess>> findOperation = findOperationTimes.apply(5);
        ControllableOperation<Set<OfferProcess>> findOperation2 = findOperationTimes.apply(1).start();
        ControllableOperation<Set<OfferProcess>> findOperation3 = findOperationTimes.apply(1).start();

        insertOperation.start();
        updateOperation.start();
        findOperation.start();

        // initial
        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("initialCount")
                .isEqualTo(initialCount);

        // init update
        updateOperation.resume();

        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterInitUpdateCount")
                .isEqualTo(afterInitUpdateCount);

        // init insert
        insertOperation.resume();

        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterInitInsertCount")
                .isEqualTo(afterInitInsertCount);

        // complete update
        updateOperation.complete();

        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterCompleteUpdateCount")
                .isEqualTo(afterCompleteUpdateCount);

        System.err.println("findOperation2 = " + findOperation2.complete().size());

        // complete insert
        insertOperation.complete();

        offerProcessList = (Set<OfferProcess>)findOperation.resume();
        assertThat(offerProcessList.size()).describedAs("afterCompleteInsertCount")
                .isEqualTo(afterCompleteInsertCount);

        System.err.println("findOperation3 = " + findOperation3.complete().size());
        System.err.println("findOperation complete = " + findOperation.complete().size());
    }

    @Test
    public void performUpdateTest_onSerializable() {
        performUpdateTest(Isolation.SERIALIZABLE);
    }

    private void performUpdateTest(
            Isolation isolationLevel
    ) {
        OfferProcess offerProcess1 = isolationLevelService.insertAndFlushOfferProcess(isolationLevel, OfferProcessStatus.OPEN).start().complete();
        OfferProcess offerProcess2 = isolationLevelService.insertAndFlushOfferProcess(isolationLevel, OfferProcessStatus.CLOSED).start().complete();
        offerProcess2.setStatus(OfferProcessStatus.OPEN);

        offerProcess2.setStatus(OfferProcessStatus.OPEN);

        Collection<OfferProcess> offerProcesses;

        ControllableOperation<OfferProcess> insertOperation = isolationLevelService.insertAndFlushOfferProcess(isolationLevel, OfferProcessStatus.OPEN);
        ControllableOperation<OfferProcess> updateOperation = isolationLevelService.updateAndFlushOfferProcess(isolationLevel, offerProcess2);
        ControllableOperation<List<OfferProcess>> findAndUpdateOperation = isolationLevelService.findAndUpdate(isolationLevel,
                OfferProcessStatus.OPEN, OfferProcessStatus.CLOSED);

        insertOperation.start();
        updateOperation.start();

        // start change transactions
        insertOperation.resume();
        updateOperation.resume();

        findAndUpdateOperation.start();

        // start find and update
        offerProcesses = (Collection<OfferProcess>)findAndUpdateOperation.resume();

        System.err.println("before update: " + offerProcesses.size());

        // complete transactions
        //insertOperation.complete();
        //updateOperation.complete();

        offerProcesses = findAndUpdateOperation.complete();

        insertOperation.complete();
        updateOperation.complete();

        System.err.println("after update: " + offerProcesses.size());

        System.err.println(offerProcessRepository.findByStatus(OfferProcessStatus.OPEN));
    }

}
