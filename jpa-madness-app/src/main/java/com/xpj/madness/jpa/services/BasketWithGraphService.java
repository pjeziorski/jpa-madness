package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.BasketWithGraph;
import com.xpj.madness.jpa.repositories.BasketWithGraphRepository;
import com.xpj.madness.jpa.repositories.BasketWithGraphRepository2;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BasketWithGraphService {

    private final BasketWithGraphRepository repository;
    private final BasketWithGraphRepository2 repository2;

    @Transactional
    public BasketWithGraph saveAndFlush(BasketWithGraph basketWithGraph) {
        return repository.saveAndFlush(basketWithGraph);
    }

    @Transactional
    public Optional<BasketWithGraph> findById(String id) {
        return repository.findById(id);
    }

    @Transactional
    public Optional<BasketWithGraph> findByIdWithEntityGraph(String id) {
        return repository2.findById(id);
    }

    @Transactional
    public Optional<BasketWithGraph> findWithQuery(String id) {
        return repository2.findWithQuery(id);
    }

    @Transactional
    public Optional<BasketWithGraph> findWithQueryAndEntityGraph(String id) {
        return repository2.findWithQueryAndEntityGraph(id);
    }

    @Transactional
    public Optional<BasketWithGraph> findWithQueryAndJoinFetch(String id) {
        return repository2.findWithQueryAndJoinFetch(id);
    }

    @Transactional
    public Optional<BasketWithGraph> findByIdAndPrintItems(String id) {
        Optional<BasketWithGraph> entityOpt = repository.findById(id);

        entityOpt.ifPresent(entity -> {
            entity.getItems().stream()
                    .forEach(System.out::println);
        });

        return entityOpt;
    }

    @Transactional
    public void withTransaction(Runnable runnable) {
        runnable.run();
    }
}
