package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.Process;
import com.xpj.madness.jpa.entities.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProcessRepository extends JpaRepository<Process, String> {

    Set<Process> findByStatus(ProcessStatus status);

    @Modifying
    @Query("UPDATE Process p SET p.status = :newStatus WHERE p.status = :currentStatus")
    int updateExistingStatuses(@Param("currentStatus") ProcessStatus currentStatus, @Param("newStatus") ProcessStatus newStatus);

}
