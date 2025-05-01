package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.services.ControllableOperation;
import com.xpj.madness.jpa.services.IsolationLevelService;
import com.xpj.madness.jpa.services.IsolationLevelService2;
import com.xpj.madness.jpa.services.TransactionalWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
public class IsolationLevelTest2 {

    @Autowired
    private IsolationLevelService2 isolationLevelService;

    @Autowired
    private OfferProcessRepository offerProcessRepository;

    @Autowired
    private TransactionalWrapper transactionalWrapper;

    @BeforeEach
    public void setUp() {
        offerProcessRepository.deleteAll();
    }

    @Test
    public void test() {
        Isolation isolationLevel = Isolation.REPEATABLE_READ;

        OfferProcess offerProcess1 = offerProcessRepository.saveAndFlush(OfferProcess.builder()
                .creationTime(OffsetDateTime.now())
                .status(OfferProcessStatus.OPEN)
                .build());
        OfferProcess offerProcess2 = offerProcessRepository.saveAndFlush(OfferProcess.builder()
                .creationTime(OffsetDateTime.now())
                .status(OfferProcessStatus.OPEN)
                .build());

        ControllableOperation<OfferProcess> listAndUpdate1 = isolationLevelService.listAndUpdate(isolationLevel, offerProcess1.getId());
        ControllableOperation<OfferProcess> listAndUpdate2 = isolationLevelService.listAndUpdate(isolationLevel, offerProcess1.getId());

        listAndUpdate1.start();
        listAndUpdate2.start();

        listAndUpdate1.resume();
        System.err.println("a");

        listAndUpdate2.resume();
        System.err.println("b");
        listAndUpdate2.resume();
        System.err.println("c");

        listAndUpdate1.resumeAsync();
        System.err.println("d");
        listAndUpdate2.complete();
    }

    @Test
    public void test2() {
        Isolation isolationLevel = Isolation.SERIALIZABLE;

        OfferProcess offerProcess1 = offerProcessRepository.saveAndFlush(OfferProcess.builder()
                .creationTime(OffsetDateTime.now())
                .status(OfferProcessStatus.OPEN)
                .build());
        OfferProcess offerProcess2 = offerProcessRepository.saveAndFlush(OfferProcess.builder()
                .creationTime(OffsetDateTime.now())
                .status(OfferProcessStatus.OPEN)
                .build());

        ControllableOperation<List<OfferProcess>> updateStatuses =
                isolationLevelService.updateStatuses(isolationLevel, OfferProcessStatus.OPEN, OfferProcessStatus.CLOSED);

        updateStatuses.start();
        updateStatuses.resume(); // list all

        transactionalWrapper.wrap(isolationLevel, () ->
        offerProcessRepository.saveAndFlush(OfferProcess.builder()
                .creationTime(OffsetDateTime.now())
                .status(OfferProcessStatus.OPEN)
                .build()));

        updateStatuses.complete();

        System.err.println(offerProcessRepository.findByStatus(OfferProcessStatus.OPEN).size());
    }
}
