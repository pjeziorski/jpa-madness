package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.services.IsolationLevelsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.xpj.madness.jpa.entities.OfferProcess;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
public class IsolationLevelsTest {

    @Autowired
    OfferProcessRepository offerProcessRepository;

    @Autowired
    IsolationLevelsService isolationLevelsService;

    @Test
    public void performOnReadUncommitted() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnReadUncommitted());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnReadCommitted() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnReadCommitted());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnReadCommitted3() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnReadCommitted3());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnRepeatableRead() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnRepeatableRead());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnSerializable() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnSerializable2());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnSerializable3() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnSerializable3());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    private List<OfferProcess> prepareExistingProcesses() {
        List<OfferProcess> processes = List.of(
                OfferProcess.builder()
                        .creationTime(OffsetDateTime.now())
                        .status(OfferProcessStatus.CLOSED)
                        .build(),
                OfferProcess.builder()
                        .creationTime(OffsetDateTime.now())
                        .status(OfferProcessStatus.OPEN)
                        .build(),
                OfferProcess.builder()
                        .creationTime(OffsetDateTime.now())
                        .status(OfferProcessStatus.CANCELLED)
                        .build()
        );

        return offerProcessRepository.saveAll(processes);
    }

    private List<OfferProcess> performParallelOperations(Supplier<OfferProcess> operation) {
        int numberOfThreads = 2;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch awaitLatch = new CountDownLatch(1);

        List<Future<OfferProcess>> futureResults = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            futureResults.add(executor.submit(() -> {
                awaitLatch.await();
                return operation.get();
            }));
        }

        awaitLatch.countDown();

        return futureResults.stream()
                .map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Exception on future.get()", e);
                    }
                })
                .collect(Collectors.toList());
    }

}
