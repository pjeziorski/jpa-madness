package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.Process;
import com.xpj.madness.jpa.entities.ProcessStatus;
import com.xpj.madness.jpa.repositories.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
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

    private final ProcessRepository processRepository;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Process performOnReadUncommitted() {
        return performOperation();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Process performOnReadCommitted() {
        return performOperation();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Process performOnReadCommitted3() {
        return performOperation3();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Process performOnRepeatableRead() {
        return performOperation();
    }

    @Retryable(
            value = CannotAcquireLockException.class,
            maxAttempts = 3
            //backoff = @Backoff(delay = 1000)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Process performOnSerializable() {
        return performOperation();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Process performOnSerializable2() {
        return performOperation2();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Process performOnSerializable3() {
        return performOperation3();
    }

    private Process performOperation() {
        markOpenProcessesAsClosed();
        return addNewOpenProcess();
    }

    private Process performOperation2() {
        markOpenProcessesAsClosed2();
        return null;
        //return addNewOpenProcess();
    }

    private Process performOperation3() {
        Process newProcess = addNewOpenProcess();

        markOpenProcessesAsClosed3(newProcess.getId());
        return newProcess;
    }

    private void markOpenProcessesAsClosed() {
        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        openProcesses.stream()
                .forEach(process -> {
                    process.setStatus(ProcessStatus.CLOSED);
                });

        processRepository.saveAll(openProcesses);
    }

    private void markOpenProcessesAsClosed2() {
        processRepository.updateExistingStatuses(ProcessStatus.OPEN, ProcessStatus.CLOSED);
    }

    private void markOpenProcessesAsClosed3(String excludeId) {
        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        openProcesses.stream()
                .filter(process -> !Objects.equals(process.getId(), excludeId))
                .forEach(process -> {
                    process.setStatus(ProcessStatus.CLOSED);
                });

        processRepository.saveAll(openProcesses);
    }

    private Process addNewOpenProcess() {
        Process process = Process.builder()
                .creationTime(OffsetDateTime.now())
                .status(ProcessStatus.OPEN)
                .build();
        return processRepository.save(process);
    }
}
