package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.RuntimeExceptionWrapper;
import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
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
    OfferProcessRepository offerProcessRepository;

    @Autowired
    DeadlockOperationService deadlockOperationService;

    @Test
    public void shouldHaveManyOpenProcesses_whenUsingReadCommitted() {
        prepareExistingProcesses();

        performParallelOperations(() -> deadlockOperationService.performOnReadCommitted());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

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

    @Test
    public void shouldHaveOneOpenProcesses_whenUsingRepeatableReadWithRetry() {
        prepareExistingProcesses();

        performParallelOperations(() -> deadlockOperationService.performOnRepeatableReadWithRetry());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);

        System.err.println("\n=== All ==");
        offerProcessRepository.findAllByOrderByCreationTimeDesc().forEach(System.err::println);

        assertThat(openProcesses.size()).isEqualTo(1);
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

    @SneakyThrows
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
