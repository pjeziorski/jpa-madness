package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.repositories.OfferProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class IsolationLevelService {

    private final OfferProcessRepository offerProcessRepository;
    private final TransactionalWrapper transactionalWrapper;
    private final IsolationLevelService isolationLevelService;

    public IsolationLevelService(OfferProcessRepository offerProcessRepository,
                                 TransactionalWrapper transactionalWrapper,
                                 @Lazy IsolationLevelService isolationLevelService) {
        this.offerProcessRepository = offerProcessRepository;
        this.transactionalWrapper = transactionalWrapper;
        this.isolationLevelService = isolationLevelService;
    }

    public ControllableOperation<Set<OfferProcess>> findByStatus(Isolation isolationLevel, OfferProcessStatus status, int readTimes) {
        return new ControllableOperation<>(
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> findByStatusTimes(ctrl, status, readTimes)));
    }

    public ControllableOperation<List<OfferProcess>> findAndUpdate(Isolation isolationLevel, OfferProcessStatus fromStatus, OfferProcessStatus toStatus) {
        return new ControllableOperation<>(
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> {
                            Set<OfferProcess> offerProcesses = (Set<OfferProcess>)ctrl.pauseBefore(
                                    () -> offerProcessRepository.findByStatus(fromStatus));

                            offerProcesses.forEach(offerProcess -> offerProcess.setStatus(toStatus));

                            return (List<OfferProcess>)ctrl.pauseBefore(
                                    () -> offerProcessRepository.saveAllAndFlush(offerProcesses));
                        }));
    }

    public ControllableOperation<Set<OfferProcess>> findByStatus_onReadUncommitted(OfferProcessStatus status, int readTimes) {
        return new ControllableOperation<>(
                (ctrl) -> isolationLevelService.findByStatus_onReadUncommitted(ctrl, status, readTimes));
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Set<OfferProcess> findByStatus_onReadUncommitted(ControllableOperation controllableOperation, OfferProcessStatus status, int readTimes) {
        return findByStatusTimes(controllableOperation, status, readTimes);
    }

    public ControllableOperation<Set<OfferProcess>> findByStatus_onReadCommitted(OfferProcessStatus status, int readTimes) {
        return new ControllableOperation<>(
                (ctrl) -> isolationLevelService.findByStatus_onReadCommitted(ctrl, status, readTimes));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Set<OfferProcess> findByStatus_onReadCommitted(ControllableOperation controllableOperation, OfferProcessStatus status, int readTimes) {
        return findByStatusTimes(controllableOperation, status, readTimes);
    }

    public ControllableOperation<Set<OfferProcess>> findByStatus_onRepeatableRead(OfferProcessStatus status, int readTimes) {
        return new ControllableOperation<>(
                (ctrl) -> isolationLevelService.findByStatus_onRepeatableRead(ctrl, status, readTimes));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Set<OfferProcess> findByStatus_onRepeatableRead(ControllableOperation controllableOperation, OfferProcessStatus status, int readTimes) {
        return findByStatusTimes(controllableOperation, status, readTimes);
    }

    public ControllableOperation<Set<OfferProcess>> findByStatus_onSerializable(OfferProcessStatus status, int readTimes) {
        return new ControllableOperation<>(
                (ctrl) -> isolationLevelService.findByStatus_onSerializable(ctrl, status, readTimes));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Set<OfferProcess> findByStatus_onSerializable(ControllableOperation controllableOperation, OfferProcessStatus status, int readTimes) {
        return findByStatusTimes(controllableOperation, status, readTimes);
    }

    public ControllableOperation<OfferProcess> insertAndFlushOfferProcess(Isolation isolationLevel, OfferProcessStatus status) {
        return new ControllableOperation<>(
                (ctrl) -> transactionalWrapper.wrap(isolationLevel, () -> {
                    OfferProcess offerProcess = OfferProcess.builder()
                            .creationTime(OffsetDateTime.now())
                            .status(status)
                            .build();

                    OfferProcess savedOfferProcess = (OfferProcess)ctrl.pauseBefore(
                            () -> offerProcessRepository.saveAndFlush(offerProcess)
                    );

                    return (OfferProcess)ctrl.pauseBefore(
                            () -> savedOfferProcess
                    );
                }));
    }

    public ControllableOperation<OfferProcess> updateAndFlushOfferProcess(Isolation isolationLevel, OfferProcess offerProcess) {
        return new ControllableOperation<>(
                (ctrl) -> transactionalWrapper.wrap(isolationLevel, () -> {
                    OfferProcess savedOfferProcess = (OfferProcess)ctrl.pauseBefore(
                            () -> offerProcessRepository.saveAndFlush(offerProcess)
                    );

                    return (OfferProcess)ctrl.pauseBefore(
                            () -> savedOfferProcess
                    );
                }));
    }

    public ControllableOperation<OfferProcess> insertAndFlushOfferProcess(OfferProcessStatus status) {
        return new ControllableOperation<>(
                (ctrl) -> isolationLevelService.insertAndFlushOfferProcess(ctrl, status));
    }

    @Transactional
    public OfferProcess insertAndFlushOfferProcess(ControllableOperation controllableOperation, OfferProcessStatus status) {
        OfferProcess offerProcess = OfferProcess.builder()
                .creationTime(OffsetDateTime.now())
                .status(status)
                .build();

        OfferProcess savedOfferProcess = (OfferProcess)controllableOperation.pauseBefore(
                () -> offerProcessRepository.saveAndFlush(offerProcess)
        );

        return (OfferProcess)controllableOperation.pauseBefore(
                () -> savedOfferProcess
        );
    }

    public ControllableOperation<OfferProcess> updateAndFlushOfferProcess(OfferProcess offerProcess) {
        return new ControllableOperation<>(
                (ctrl) -> isolationLevelService.updateAndFlushOfferProcess(ctrl, offerProcess));
    }

    @Transactional
    public OfferProcess updateAndFlushOfferProcess(ControllableOperation controllableOperation, OfferProcess offerProcess) {
        OfferProcess savedOfferProcess = (OfferProcess)controllableOperation.pauseBefore(
                () -> offerProcessRepository.saveAndFlush(offerProcess)
        );

        return (OfferProcess)controllableOperation.pauseBefore(
                () -> savedOfferProcess
        );
    }

    private Set<OfferProcess> findByStatusTimes(ControllableOperation controllableOperation, OfferProcessStatus status, int readTimes) {
        Set<OfferProcess> lastResult = null;

        for (int i = 0; i < readTimes; i++) {
            lastResult = (Set<OfferProcess>)controllableOperation.pauseBefore(
                    () -> offerProcessRepository.findByStatus(status)
            );
        }
        return lastResult;
    }


}
