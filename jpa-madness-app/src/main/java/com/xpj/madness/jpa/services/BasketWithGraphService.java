package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.BasketWithGraph;
import com.xpj.madness.jpa.repositories.BasketWithGraphRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BasketWithGraphService {

    private final BasketWithGraphRepository repository;

    @Transactional
    public BasketWithGraph saveAndFlush(BasketWithGraph basketWithGraph) {
        return repository.saveAndFlush(basketWithGraph);
    }

    @Transactional
    public Optional<BasketWithGraph> findById(String id) {
        return repository.findById(id);
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
}
