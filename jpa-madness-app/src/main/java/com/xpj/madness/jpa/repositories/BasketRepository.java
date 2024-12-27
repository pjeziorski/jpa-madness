package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketRepository extends JpaRepository<Basket, String> {
}
