package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.services.ControllableOperation;
import com.xpj.madness.jpa.services.IsolationLevelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
public class IsolationLevelTest {

    @Autowired
    private IsolationLevelService isolationLevelService;

    @Test
    public void shouldGetUncommitted_onReadUncommitted() {
        List<OfferProcess> offerProcessList;

        ControllableOperation<OfferProcess> saveOperation = isolationLevelService.saveAndFlushOfferProcess();
        ControllableOperation<List<OfferProcess>> findAllOperation = isolationLevelService.findAll_onReadUncommitted();

        saveOperation.start();
        findAllOperation.start();

        // save without commit
        saveOperation.resume();

        offerProcessList = (List<OfferProcess>)findAllOperation.resume();
        assertThat(offerProcessList.size()).isEqualTo(1);

        // do save commit
        saveOperation.resume();
        saveOperation.getResult();

        offerProcessList = (List<OfferProcess>)findAllOperation.resume();
        assertThat(offerProcessList.size()).isEqualTo(1);
    }

    @Test
    public void shouldNotGetUncommitted_onReadCommitted() {
        List<OfferProcess> offerProcessList;

        ControllableOperation<OfferProcess> saveOperation = isolationLevelService.saveAndFlushOfferProcess();
        ControllableOperation<List<OfferProcess>> findAllOperation = isolationLevelService.findAll_onReadCommitted();

        saveOperation.start();
        findAllOperation.start();

        // save without commit
        saveOperation.resume();

        offerProcessList = (List<OfferProcess>)findAllOperation.resume();
        assertThat(offerProcessList.size()).isEqualTo(0);

        // do save commit
        saveOperation.resume();
        saveOperation.getResult();

        offerProcessList = (List<OfferProcess>)findAllOperation.resume();
        assertThat(offerProcessList.size()).isEqualTo(1);
    }

}
