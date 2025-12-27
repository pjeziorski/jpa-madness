package com.xpj.madness.jpa.legacy.repositories;

import com.xpj.madness.jpa.legacy.entities.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketRepository extends JpaRepository<Basket, String> {
}
