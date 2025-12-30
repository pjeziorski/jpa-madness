package com.xpj.madness.jpa.peristance.inheritance.repository;

import com.xpj.madness.jpa.peristance.inheritance.entity.UC08SeparateParent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface UC08SeparateParentRepository extends JpaRepository<UC08SeparateParent, Long> {

    Collection<UC08SeparateParent> findAllByTestId(String testId);

}
