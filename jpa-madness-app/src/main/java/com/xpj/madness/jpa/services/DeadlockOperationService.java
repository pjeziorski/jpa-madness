package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.repositories.OfferProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeadlockOperationService {

    private final OfferProcessRepository offerProcessRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OfferProcess performOnReadCommitted() {
        return performOperation();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OfferProcess performOnRepeatableRead() {
        return performOperation();
    }

    @Retryable(
            value = CannotAcquireLockException.class,
            maxAttempts = 3 // this is crucial in defining how many parallel executions can happen
    )
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OfferProcess performOnRepeatableReadWithRetry() {
        return performOperation();
    }

    @Transactional
    public OfferProcess performOperationWithChangedOrderOnDefaultLevel() {
        return performOperationWithChangedOrder();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OfferProcess performOperationWithChangedOrderOnRepeatableRead() {
        return performOperationWithChangedOrder();
    }

    @Transactional
    public void changeStatusesWithSingleQuery() {
        markOpenProcessesAsClosedWthSingleQuery();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void changeStatusesWithSingleQueryOnRepeatableRead() {
        markOpenProcessesAsClosedWthSingleQuery();
    }

    private OfferProcess performOperation() {
        markOpenProcessesAsClosed();
        return addNewOpenProcess();
    }

    private OfferProcess performOperationWithChangedOrder() {
        OfferProcess process = addNewOpenProcess();
        markOpenProcessesAsClosed(process.getId());
        return process;
    }

    private void markOpenProcessesAsClosed() {
        Set<OfferProcess> openOfferProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        openOfferProcesses.stream()
                .forEach(process -> {
                    process.setStatus(OfferProcessStatus.CLOSED);
                });

        offerProcessRepository.saveAll(openOfferProcesses);
    }

    private void markOpenProcessesAsClosed(String excludeId) {
        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        openProcesses.stream()
                .filter(process -> !Objects.equals(process.getId(), excludeId))
                .forEach(process -> {
                    process.setStatus(OfferProcessStatus.CLOSED);
                });

        offerProcessRepository.saveAll(openProcesses);
    }

    private void markOpenProcessesAsClosedWthSingleQuery() {
        offerProcessRepository.updateExistingStatuses(OfferProcessStatus.OPEN, OfferProcessStatus.CLOSED);
    }

    private OfferProcess addNewOpenProcess() {
        OfferProcess process = OfferProcess.builder()
                .creationTime(OffsetDateTime.now())
                .status(OfferProcessStatus.OPEN)
                .build();
        return offerProcessRepository.save(process);
    }

}
