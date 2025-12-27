package com.xpj.madness.jpa.legacy.services;

import com.xpj.madness.jpa.legacy.entities.OfferProcess;
import com.xpj.madness.jpa.legacy.entities.OfferProcessStatus;
import com.xpj.madness.jpa.legacy.repositories.OfferProcessRepository;
import com.xpj.madness.jpa.utils.ControllableOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SingleOpenOfferService {

    private final OfferProcessRepository offerProcessRepository;
    private final TransactionalWrapper transactionalWrapper;

    public ControllableOperation<OfferProcess> insertNew(Isolation isolationLevel, OfferProcess offerProcess) {
        return new ControllableOperation<>(
                "insertNew",
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> {
                            Set<OfferProcess> openOfferProcesses = ctrl.pauseBefore("findOpenProcesses",
                                    () -> offerProcessRepository.findByStatus(OfferProcessStatus.OPEN));

                            openOfferProcesses.forEach(op -> op.setStatus(OfferProcessStatus.CLOSED));

                            ctrl.pauseBefore("updateOpenProcesses",
                                    () -> offerProcessRepository.saveAllAndFlush(openOfferProcesses));

                            OfferProcess savedOfferProcess = ctrl.pauseBefore("saveNewOfferProcess",
                                    () -> offerProcessRepository.saveAndFlush(offerProcess));

                            return ctrl.pauseBefore("commit", () -> savedOfferProcess);
                        })
        );
    }

    public ControllableOperation<OfferProcess> insertNewWithSingleUpdate(Isolation isolationLevel, OfferProcess offerProcess) {
        return new ControllableOperation<>(
                "insertNewWithSingleUpdate",
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> {
                            ctrl.pauseBefore("updateExistingStatuses",
                                    () -> offerProcessRepository.updateExistingStatuses(OfferProcessStatus.OPEN, OfferProcessStatus.CANCELLED));

                            OfferProcess savedOfferProcess = ctrl.pauseBefore("saveNewOfferProcess",
                                    () -> offerProcessRepository.saveAndFlush(offerProcess));

                            return ctrl.pauseBefore("commit", () -> savedOfferProcess);
                        })
        );
    }

    public ControllableOperation<OfferProcess> insertNewWithSeparateTransactions(Isolation isolationLevel, OfferProcess offerProcess) {
        return new ControllableOperation<>(
                "insertNewWithSeparateTransactions",
                (ctrl) -> {
                    OfferProcess savedOfferProcess = ctrl.pauseBefore("saveNewOfferProcess",
                            () -> offerProcessRepository.saveAndFlush(offerProcess));

                    transactionalWrapper.wrap(isolationLevel,
                            () -> {
                                Set<OfferProcess> openOfferProcesses = ctrl.pauseBefore("findOpenProcesses",
                                        () -> offerProcessRepository.findByStatusWithLock(OfferProcessStatus.OPEN));

                                openOfferProcesses.removeIf(op -> op.getId().equals(savedOfferProcess.getId()));
                                openOfferProcesses.forEach(op -> op.setStatus(OfferProcessStatus.CLOSED));

                                ctrl.pauseBefore("updateOpenProcesses",
                                        () -> offerProcessRepository.saveAllAndFlush(openOfferProcesses));

                                return ctrl.pauseBefore("commit", () -> savedOfferProcess);
                            });

                    return savedOfferProcess;
                }
        );

    }

    public ControllableOperation<OfferProcess> insertNewWithSeparateTransactionsAndSingleUpdate(Isolation isolationLevel, OfferProcess offerProcess) {
        return new ControllableOperation<>(
                "insertNewWithSeparateTransactionsAndSingleUpdate",
                (ctrl) -> {
                    OfferProcess savedOfferProcess = ctrl.pauseBefore("saveNewOfferProcess",
                            () -> offerProcessRepository.saveAndFlush(offerProcess));

                    transactionalWrapper.wrap(isolationLevel,
                            () -> {
                                ctrl.pauseBefore("",
                                        () -> offerProcessRepository.updateExistingStatusesExcluding(
                                                OfferProcessStatus.OPEN, OfferProcessStatus.CLOSED, savedOfferProcess.getId()));

                                return ctrl.pauseBefore("commit", () -> savedOfferProcess);
                            });

                    return savedOfferProcess;
                }
        );
    }

}
