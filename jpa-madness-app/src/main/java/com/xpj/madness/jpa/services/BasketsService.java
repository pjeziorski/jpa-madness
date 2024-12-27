package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.Basket;
import com.xpj.madness.jpa.repositories.BasketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BasketsService {

    private final BasketRepository repository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Basket saveAndFlush(Basket basket) {
        return repository.saveAndFlush(basket);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<Basket> findById(String id) {
        return repository.findById(id);
    }

}
