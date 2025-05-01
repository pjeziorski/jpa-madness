package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.services.ControllableOperation;
import com.xpj.madness.jpa.services.IsolationLevelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
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

}
