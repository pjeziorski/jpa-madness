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
public class IsolationLevelsService {

    private final OfferProcessRepository offerProcessRepository;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public OfferProcess performOnReadUncommitted() {
        return performOperation();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OfferProcess performOnReadCommitted() {
        return performOperation();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OfferProcess performOnReadCommitted3() {
        return performOperation3();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OfferProcess performOnRepeatableRead() {
        return performOperation();
    }

    @Retryable(
            value = CannotAcquireLockException.class,
            maxAttempts = 3
            //backoff = @Backoff(delay = 1000)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OfferProcess performOnSerializable() {
        return performOperation();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OfferProcess performOnSerializable2() {
        return performOperation2();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OfferProcess performOnSerializable3() {
        return performOperation3();
    }

    private OfferProcess performOperation() {
        markOpenProcessesAsClosed();
        return addNewOpenProcess();
    }

    private OfferProcess performOperation2() {
        markOpenProcessesAsClosed2();
        return null;
        //return addNewOpenProcess();
    }

    private OfferProcess performOperation3() {
        OfferProcess newProcess = addNewOpenProcess();

        markOpenProcessesAsClosed3(newProcess.getId());
        return newProcess;
    }

    private void markOpenProcessesAsClosed() {
        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        openProcesses.stream()
                .forEach(process -> {
                    process.setStatus(OfferProcessStatus.CLOSED);
                });

        offerProcessRepository.saveAll(openProcesses);
    }

    private void markOpenProcessesAsClosed2() {
        offerProcessRepository.updateExistingStatuses(OfferProcessStatus.OPEN, OfferProcessStatus.CLOSED);
    }

    private void markOpenProcessesAsClosed3(String excludeId) {
        Set<OfferProcess> openProcesses = offerProcessRepository.findByStatus(OfferProcessStatus.OPEN);

        openProcesses.stream()
                .filter(process -> !Objects.equals(process.getId(), excludeId))
                .forEach(process -> {
                    process.setStatus(OfferProcessStatus.CLOSED);
                });

        offerProcessRepository.saveAll(openProcesses);
    }

    private OfferProcess addNewOpenProcess() {
        OfferProcess process = OfferProcess.builder()
                .creationTime(OffsetDateTime.now())
                .status(OfferProcessStatus.OPEN)
                .build();
        return offerProcessRepository.save(process);
    }
}
