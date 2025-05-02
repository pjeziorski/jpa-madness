package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.repositories.OfferProcessRepository;
import com.xpj.madness.jpa.utils.ControllableOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class IsolationLevelOnReadsService {

    private final OfferProcessRepository offerProcessRepository;
    private final TransactionalWrapper transactionalWrapper;

    public ControllableOperation<Set<OfferProcess>> findByStatus(Isolation isolationLevel, OfferProcessStatus status, int readTimes) {
        return new ControllableOperation<>(
                "findByStatus-" + isolationLevel,
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> findByStatusTimes(ctrl, status, readTimes)));
    }

    public ControllableOperation<OfferProcess> insertAndFlush(Isolation isolationLevel, OfferProcess offerProcess) {
        return saveAndFlush("insertAndFlush", isolationLevel, offerProcess);
    }

    public ControllableOperation<OfferProcess> updateAndFlush(Isolation isolationLevel, OfferProcess offerProcess) {
        return saveAndFlush("updateAndFlush", isolationLevel, offerProcess);
    }

    private ControllableOperation<OfferProcess> saveAndFlush(String saveName, Isolation isolationLevel, OfferProcess offerProcess) {
        return new ControllableOperation<>(
                saveName + "-" + isolationLevel,
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> {
                            OfferProcess savedOfferProcess = ctrl.pauseBefore("saveAndFlush", () ->
                                    offerProcessRepository.saveAndFlush(offerProcess));

                            return ctrl.pauseBefore("commit", () -> savedOfferProcess);
                        }));
    }

    private Set<OfferProcess> findByStatusTimes(ControllableOperation<?> controllableOperation, OfferProcessStatus status, int readTimes) {
        Set<OfferProcess> lastResult = null;

        for (int i = 0; i < readTimes; i++) {
            lastResult = controllableOperation.pauseBefore(
                    "findByStatus_" + (i+1) + "_of_" + readTimes,
                    () -> offerProcessRepository.findByStatus(status)
            );
        }
        return lastResult;
    }


}
