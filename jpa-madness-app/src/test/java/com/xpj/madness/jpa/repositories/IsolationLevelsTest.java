package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.ProcessStatus;
import com.xpj.madness.jpa.services.IsolationLevelsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.xpj.madness.jpa.entities.Process;

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
    ProcessRepository processRepository;

    @Autowired
    IsolationLevelsService isolationLevelsService;

    @Test
    public void performOnReadUncommitted() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnReadUncommitted());

        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnReadCommitted() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnReadCommitted());

        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnReadCommitted3() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnReadCommitted3());

        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnRepeatableRead() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnRepeatableRead());

        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnSerializable() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnSerializable2());

        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    @Test
    public void performOnSerializable3() {
        prepareExistingProcesses();

        performParallelOperations(() -> isolationLevelsService.performOnSerializable3());

        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);
    }

    private List<Process> prepareExistingProcesses() {
        List<Process> processes = List.of(
                Process.builder()
                        .creationTime(OffsetDateTime.now())
                        .status(ProcessStatus.CLOSED)
                        .build(),
                Process.builder()
                        .creationTime(OffsetDateTime.now())
                        .status(ProcessStatus.OPEN)
                        .build(),
                Process.builder()
                        .creationTime(OffsetDateTime.now())
                        .status(ProcessStatus.CANCELLED)
                        .build()
        );

        return processRepository.saveAll(processes);
    }

    private List<Process> performParallelOperations(Supplier<Process> operation) {
        int numberOfThreads = 2;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch awaitLatch = new CountDownLatch(1);

        List<Future<Process>> futureResults = new ArrayList<>();

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
