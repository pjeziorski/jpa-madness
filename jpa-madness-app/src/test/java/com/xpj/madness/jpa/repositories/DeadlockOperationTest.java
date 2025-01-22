package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.RuntimeExceptionWrapper;
import com.xpj.madness.jpa.entities.Process;
import com.xpj.madness.jpa.entities.ProcessStatus;
import com.xpj.madness.jpa.services.DeadlockOperationService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
public class DeadlockOperationTest {

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    DeadlockOperationService deadlockOperationService;

    @Test
    public void shouldHaveManyOpenProcesses_whenUsingReadCommitted() {
        prepareExistingProcesses();

        performParallelOperations(() -> deadlockOperationService.performOnReadCommitted());

        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);

        assertThat(openProcesses.size()).isGreaterThan(1);
    }

    @Test
    public void shouldThrowDeadlock_whenUsingRepeatableRead() {
        prepareExistingProcesses();

        assertThatExceptionOfType(CannotAcquireLockException.class).isThrownBy(
                    () -> performParallelOperations(() -> deadlockOperationService.performOnRepeatableRead())
                )
                .satisfies(ex -> ex.printStackTrace());
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

    @SneakyThrows
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

        try {
            return futureResults.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new RuntimeExceptionWrapper("Exception on future.get()", e);
                        }
                    })
                    .collect(Collectors.toList());
        }
        catch (RuntimeExceptionWrapper e) {
            Throwable cause = e.getCause();

            if (cause instanceof ExecutionException) {
                throw cause.getCause();
            }
            else {
                throw cause;
            }
        }
    }
}
