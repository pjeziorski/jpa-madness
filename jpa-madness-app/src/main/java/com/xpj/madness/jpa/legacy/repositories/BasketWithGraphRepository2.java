package com.xpj.madness.jpa.legacy.repositories;

import com.xpj.madness.jpa.legacy.entities.BasketWithGraph;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketWithGraphRepository2 extends JpaRepository<BasketWithGraph, String> {

    @Override
    @EntityGraph("allRelations")
    Optional<BasketWithGraph> findById(String id);

    @Query("SELECT b FROM BasketWithGraph b WHERE b.id=:id")
    Optional<BasketWithGraph> findWithQuery(@Param("id") String id);

    @EntityGraph("allRelations")
    @Query("SELECT b FROM BasketWithGraph b WHERE b.id=:id")
    Optional<BasketWithGraph> findWithQueryAndEntityGraph(@Param("id") String id);

    @Query("SELECT b FROM BasketWithGraph b JOIN FETCH b.items WHERE b.id=:id")
    Optional<BasketWithGraph> findWithQueryAndJoinFetch(@Param("id") String id);
}
