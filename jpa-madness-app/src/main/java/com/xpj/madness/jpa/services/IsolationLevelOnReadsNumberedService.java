package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.NumberedOfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.repositories.NumberedOfferProcessRepository;
import com.xpj.madness.jpa.utils.ControllableOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class IsolationLevelOnReadsNumberedService {

    private final NumberedOfferProcessRepository numberedOfferProcessRepository;
    private final TransactionalWrapper transactionalWrapper;

    public ControllableOperation<Set<NumberedOfferProcess>> findByStatus(Isolation isolationLevel, OfferProcessStatus status, int readTimes) {
        return new ControllableOperation<>(
                "findByStatus-" + isolationLevel,
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> findByStatusTimes(ctrl, status, readTimes)));
    }

    public ControllableOperation<NumberedOfferProcess> insertAndFlush(Isolation isolationLevel, NumberedOfferProcess offerProcess) {
        return saveAndFlush("insertAndFlush", isolationLevel, offerProcess);
    }

    public ControllableOperation<NumberedOfferProcess> updateAndFlush(Isolation isolationLevel, NumberedOfferProcess offerProcess) {
        return saveAndFlush("updateAndFlush", isolationLevel, offerProcess);
    }

    private ControllableOperation<NumberedOfferProcess> saveAndFlush(String saveName, Isolation isolationLevel, NumberedOfferProcess offerProcess) {
        return new ControllableOperation<>(
                saveName + "-" + isolationLevel,
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> {
                            NumberedOfferProcess savedOfferProcess = ctrl.pauseBefore("saveAndFlush", () ->
                                    numberedOfferProcessRepository.saveAndFlush(offerProcess));

                            return ctrl.pauseBefore("commit", () -> savedOfferProcess);
                        }));
    }

    private Set<NumberedOfferProcess> findByStatusTimes(ControllableOperation<?> controllableOperation, OfferProcessStatus status, int readTimes) {
        Set<NumberedOfferProcess> lastResult = null;

        for (int i = 0; i < readTimes; i++) {
            lastResult = controllableOperation.pauseBefore(
                    "findByStatus_" + (i+1) + "_of_" + readTimes,
                    () -> numberedOfferProcessRepository.findByBalanceGreaterThan(100)
            );
        }
        return lastResult;
    }


}
