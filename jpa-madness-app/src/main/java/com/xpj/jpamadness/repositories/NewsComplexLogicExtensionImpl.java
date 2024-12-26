package com.xpj.jpamadness.repositories;

import com.xpj.jpamadness.entities.News;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NewsComplexLogicExtensionImpl implements NewsComplexLogicExtension {

    private final EntityManager entityManager;

    @Override
    public void transactionWithSave() {
        News news1 = News.builder()
                .title("a")
                .build();

        entityManager.persist(news1);

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        entityManager.persist(news2);
    }

    @Override
    public void transactionWithSaveAndFlush() {
        News news1 = News.builder()
                .title("a")
                .build();

        entityManager.persist(news1);
        entityManager.flush();

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        entityManager.persist(news2);
        entityManager.flush();
    }

    @Override
    public void privateTransactionWithSaveAndFlush() {
        doPrivateTransactionWithSaveAndFlush();
    }

    private void doPrivateTransactionWithSaveAndFlush() {
        News news1 = News.builder()
                .title("a")
                .build();

        entityManager.persist(news1);
        entityManager.flush();

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        entityManager.persist(news2);
        entityManager.flush();
    }
}
