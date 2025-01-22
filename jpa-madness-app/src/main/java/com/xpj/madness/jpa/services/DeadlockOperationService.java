package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.Process;
import com.xpj.madness.jpa.entities.ProcessStatus;
import com.xpj.madness.jpa.repositories.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeadlockOperationService {

    private final ProcessRepository processRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Process performOnReadCommitted() {
        return performOperation();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Process performOnRepeatableRead() {
        return performOperation();
    }

    private Process performOperation() {
        markOpenProcessesAsClosed();
        return addNewOpenProcess();
    }

    private void markOpenProcessesAsClosed() {
        Set<Process> openProcesses = processRepository.findByStatus(ProcessStatus.OPEN);

        openProcesses.stream()
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
