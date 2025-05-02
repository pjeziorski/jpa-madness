package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.NumberedOfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface NumberedOfferProcessRepository extends JpaRepository<NumberedOfferProcess, Long> {

    Set<NumberedOfferProcess> findByStatus(OfferProcessStatus status);

    Set<NumberedOfferProcess> findByBalanceGreaterThan(int amount);
}
