package com.xpj.madness.jpa.peristance.inheritance.repository;

import com.xpj.madness.jpa.peristance.inheritance.entity.UC08JoinedColumnParent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface UC08JoinedColumnParentRepository extends JpaRepository<UC08JoinedColumnParent, Long> {

    Collection<UC08JoinedColumnParent> findAllByTestId(String testId);

}
