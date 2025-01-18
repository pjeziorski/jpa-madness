package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.Process;
import com.xpj.madness.jpa.entities.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProcessRepository extends JpaRepository<Process, String> {

    Set<Process> findByStatus(ProcessStatus status);

}
