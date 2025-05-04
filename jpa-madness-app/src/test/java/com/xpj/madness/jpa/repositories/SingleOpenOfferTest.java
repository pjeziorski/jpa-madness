package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.services.SingleOpenOfferService;
import com.xpj.madness.jpa.utils.ControllableOperation;
import com.xpj.madness.jpa.utils.ControllableOperationExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class SingleOpenOfferTest {

    @Autowired
    private OfferProcessRepository offerProcessRepository;

    @Autowired
    private SingleOpenOfferService singleOpenOfferService;

    @Test
    public void performTestOnEmptyDatabase() {
        performTestOnEmptyDatabase(Isolation.REPEATABLE_READ);
    }

    @BeforeEach
    public void setUp() {
        offerProcessRepository.deleteAll();
    }

    private void performTestOnEmptyDatabase(Isolation isolationLevel) {
        ControllableOperation<OfferProcess> insertNew1 = singleOpenOfferService.insertNewWithSeparateTransactions(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN));
        ControllableOperation<OfferProcess> insertNew2 = singleOpenOfferService.insertNewWithSeparateTransactions(isolationLevel, createOfferProcess(OfferProcessStatus.OPEN));

        insertNew1.start();
        insertNew2.start();

        ControllableOperationExecutor executor = new ControllableOperationExecutor();

        executor.completeAlternately(insertNew1, insertNew2);

        offerProcessRepository.findAll().forEach(System.err::println);
    }

    private OfferProcess createOfferProcess(OfferProcessStatus status) {
        return OfferProcess.builder()
                .status(status)
                .creationTime(OffsetDateTime.now())
                .build();
    }
}
