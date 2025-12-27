package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.RuntimeExceptionWrapper;
import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.services.DeadlockOperationService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("legacy")
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class DeadlockOperationTest {

    @Autowired
    OfferProcessRepository offerProcessRepository;

    @Autowired
    DeadlockOperationService deadlockOperationService;

    @BeforeEach
    public void beforeEach() {
        offerProcessRepository.deleteAll();
    }

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

        System.err.println("\n=== All ==");
        offerProcessRepository.findAllByOrderByCreationTimeDesc().forEach(System.err::println);
    }

    @Test
    public void shouldHaveOneOpenProcesses_whenUsingRepeatableReadWithRetry() {
        prepareExistingProcesses();

        List<OfferProcess> addedEntries = performParallelOperations(() -> deadlockOperationService.performOnRepeatableReadWithRetry());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);

        System.err.println("\n=== All ==");
        List<OfferProcess> processes = offerProcessRepository.findAllByOrderByCreationTimeDesc();

        processes.forEach(System.err::println);

        assertThat(openProcesses.size()).isEqualTo(1);

        // 3 existing processes + added
        assertThat(processes.size()).isEqualTo(3 + addedEntries.size());
    }

    /**
     * This test is to prove that using single query to modify existing statuses will not help
     */
    @Test
    public void shouldThrowDeadlock_whenUsingSingleQueryToChangeStatuses() {
        prepareExistingProcesses();

        performParallelOperations(() -> {
            deadlockOperationService.changeStatusesWithSingleQuery();
            return null;
        });

        assertThat(offerProcessRepository.findByStatus(OfferProcessStatus.OPEN)).isEmpty();

        prepareExistingProcesses();

        assertThatExceptionOfType(CannotAcquireLockException.class).isThrownBy(
                        () -> performParallelOperations(() -> {
                            deadlockOperationService.changeStatusesWithSingleQueryOnRepeatableRead();
                            return null;
                        })
                )
                .satisfies(ex -> ex.printStackTrace());

        System.err.println("\n=== All ==");
        offerProcessRepository.findAllByOrderByCreationTimeDesc().forEach(System.err::println);

        // even though some of the threads may throw error, one transaction should pass
        assertThat(offerProcessRepository.findByStatus(OfferProcessStatus.OPEN)).isEmpty();
    }

    @Test
    public void shouldHaveSameResults_whenChangingOrderOfOperations() {
        // on default isolation level
        prepareExistingProcesses();

        performParallelOperations(() -> deadlockOperationService.performOperationWithChangedOrderOnDefaultLevel());

        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        System.err.println(openProcesses.size());
        openProcesses.forEach(System.err::println);

        assertThat(openProcesses.size()).isGreaterThan(1);

        // on repeatable read
        offerProcessRepository.deleteAll();

        prepareExistingProcesses();

        assertThatExceptionOfType(CannotAcquireLockException.class).isThrownBy(
                        () -> performParallelOperations(() -> {
                            deadlockOperationService.performOperationWithChangedOrderOnRepeatableRead();
                            return null;
                        })
                )
                .satisfies(ex -> ex.printStackTrace());

        System.err.println("\n=== All ==");
        List<OfferProcess> processes = offerProcessRepository.findAllByOrderByCreationTimeDesc();

        processes.forEach(System.err::println);

        // 3 existing processes + only 1 that did not have deadlock
        assertThat(processes.size()).isEqualTo(4);
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
        int numberOfThreads = 3;

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

        // await all to finish
        futureResults.stream().forEach(future -> {
            try {
                future.get();
            }
            catch (Exception e) {
                // do nothing
            }
        });

        // collect results
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
