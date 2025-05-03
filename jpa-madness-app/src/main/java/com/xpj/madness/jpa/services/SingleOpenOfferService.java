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

}
