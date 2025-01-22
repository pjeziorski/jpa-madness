package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.OfferProcess;
import com.xpj.madness.jpa.entities.OfferProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OfferProcessRepository extends JpaRepository<OfferProcess, String> {

    Set<OfferProcess> findByStatus(OfferProcessStatus status);

    List<OfferProcess> findAllByOrderByCreationTimeDesc();

    @Modifying
    @Query("UPDATE OfferProcess p SET p.status = :newStatus WHERE p.status = :currentStatus")
    int updateExistingStatuses(@Param("currentStatus") OfferProcessStatus currentStatus, @Param("newStatus") OfferProcessStatus newStatus);

}
