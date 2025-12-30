package com.xpj.madness.jpa.peristance.inheritance.repository;

import com.xpj.madness.jpa.peristance.inheritance.entity.UC08JoinedParent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface UC08JoinedParentRepository extends JpaRepository<UC08JoinedParent, Long> {

    Collection<UC08JoinedParent> findAllByTestId(String testId);

}
