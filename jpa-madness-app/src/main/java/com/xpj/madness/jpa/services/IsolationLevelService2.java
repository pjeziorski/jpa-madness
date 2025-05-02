package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import com.xpj.madness.jpa.repositories.OfferProcessRepository;
import com.xpj.madness.jpa.utils.ControllableOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IsolationLevelService2 {

    private final OfferProcessRepository offerProcessRepository;
    private final TransactionalWrapper transactionalWrapper;

    public ControllableOperation<OfferProcess> listAndUpdate(Isolation isolationLevel, String idToUpdate) {
        return new ControllableOperation<>("listAndUpdate-" + isolationLevel,
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                () -> {
                    OfferProcess offerProcess = (OfferProcess)ctrl.pauseBefore("listAndFind", () -> listAndFind(idToUpdate));

                    OfferProcess savedOfferProcess = (OfferProcess)ctrl.pauseBefore("saveAndFlush", () -> {
                        OfferProcess sop = offerProcessRepository.saveAndFlush(offerProcess);
                        //offerProcessRepository.findAll();
                        return sop;
                    });

                    return (OfferProcess)ctrl.pauseBefore("commit", () -> savedOfferProcess);
                }));
    }

    private OfferProcess listAndFind(String idToUpdate) {
        List<OfferProcess> offerProcesses = offerProcessRepository.findAll();

        OfferProcess offerProcess = offerProcesses.stream().filter(op -> op.getId().equals(idToUpdate))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No offerProcess with id=" + idToUpdate));

        offerProcess.setStatus(OfferProcessStatus.CANCELLED);
        return offerProcess;
    }

    public ControllableOperation<List<OfferProcess>> updateStatuses(Isolation isolationLevel, OfferProcessStatus fromStatus, OfferProcessStatus toStatus) {
        return new ControllableOperation<>("updateStatuses-" + isolationLevel,
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                () -> {
                    Set<OfferProcess> offersToUpdate = (Set<OfferProcess>)ctrl.pauseBefore("find from Status",
                            () -> offerProcessRepository.findByStatus(fromStatus));

                    offersToUpdate.forEach(offerProcess -> offerProcess.setStatus(toStatus));

                    /*List<OfferProcess> savedOfferProcesses = (List<OfferProcess>)ctrl.pauseBefore("saveAllAndFlush",
                            () -> offerProcessRepository.saveAllAndFlush(offersToUpdate));

                    return (List<OfferProcess>)ctrl.pauseBefore(() -> savedOfferProcesses);*/

                    ctrl.pauseBefore("updateExistingStatuses",
                            () -> offerProcessRepository.updateExistingStatuses(fromStatus, toStatus));

                    return (List<OfferProcess>)ctrl.pauseBefore("commit", () -> new ArrayList(offersToUpdate));
                }));
    }

    public ControllableOperation<OfferProcess> insertAndFlush(Isolation isolationLevel, OfferProcess offerProcess) {
        return new ControllableOperation<>("insertAndFlush-" + isolationLevel,
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                () -> {
//                    ctrl.pauseBefore("find from Status",
//                            () -> offerProcessRepository.findByStatus(offerProcess.getStatus()));

                    OfferProcess savedOfferProcess = (OfferProcess)ctrl.pauseBefore("saveAndFlush", () ->
                        offerProcessRepository.saveAndFlush(offerProcess));

                    return (OfferProcess)ctrl.pauseBefore("commit", () -> savedOfferProcess);
                }));
    }
}
