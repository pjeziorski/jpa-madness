package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.repositories.OfferProcessRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class IsolationLevelService {

    private final OfferProcessRepository offerProcessRepository;
    private final IsolationLevelService isolationLevelService;

    public IsolationLevelService(OfferProcessRepository offerProcessRepository,
                                 @Lazy IsolationLevelService isolationLevelService) {
        this.offerProcessRepository = offerProcessRepository;
        this.isolationLevelService = isolationLevelService;
    }

    public ControllableOperation<List<OfferProcess>> findAll_onReadUncommitted() {
        return new ControllableOperation<>(
                (ctrl) -> isolationLevelService.findAll_onReadUncommitted(ctrl));
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<OfferProcess> findAll_onReadUncommitted(ControllableOperation controllableOperation) {
        return findAllTwice(controllableOperation);
    }

    public ControllableOperation<List<OfferProcess>> findAll_onReadCommitted() {
        return new ControllableOperation<>(
                (ctrl) -> isolationLevelService.findAll_onReadCommitted(ctrl));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<OfferProcess> findAll_onReadCommitted(ControllableOperation controllableOperation) {
        return findAllTwice(controllableOperation);
    }

    @Transactional
    public ControllableOperation<OfferProcess> saveAndFlushOfferProcess() {
        return new ControllableOperation<>(
                (ctrl) -> isolationLevelService.saveAndFlushOfferProcess(ctrl));
    }

    @Transactional
    public OfferProcess saveAndFlushOfferProcess(ControllableOperation controllableOperation) {
        OfferProcess offerProcess = OfferProcess.builder()
                .creationTime(OffsetDateTime.now())
                .status(OfferProcessStatus.OPEN)
                .build();

        OfferProcess savedOfferProcess = (OfferProcess)controllableOperation.pauseBefore(
                () -> offerProcessRepository.saveAndFlush(offerProcess)
        );

        return (OfferProcess)controllableOperation.pauseBefore(
                () -> savedOfferProcess
        );
    }

    private List<OfferProcess> findAllTwice(ControllableOperation controllableOperation) {
        controllableOperation.pauseBefore(
                () -> offerProcessRepository.findAll()
        );

        return (List<OfferProcess>)controllableOperation.pauseBefore(
                () -> offerProcessRepository.findAll()
        );
    }


}
