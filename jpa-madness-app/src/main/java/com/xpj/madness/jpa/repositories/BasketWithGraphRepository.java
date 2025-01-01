package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.BasketWithGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketWithGraphRepository extends JpaRepository<BasketWithGraph, String> {
}
