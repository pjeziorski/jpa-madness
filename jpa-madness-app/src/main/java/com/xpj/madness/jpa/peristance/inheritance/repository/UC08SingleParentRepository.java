package com.xpj.madness.jpa.peristance.inheritance.repository;

import com.xpj.madness.jpa.peristance.inheritance.entity.UC08SingleParent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface UC08SingleParentRepository extends JpaRepository<UC08SingleParent, Long> {

    Collection<UC08SingleParent> findAllByTestId(String testId);

}
