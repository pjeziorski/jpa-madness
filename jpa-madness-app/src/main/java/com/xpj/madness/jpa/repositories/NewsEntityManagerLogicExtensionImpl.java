package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.News;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NewsEntityManagerLogicExtensionImpl implements NewsEntityManagerLogicExtension {

    private final EntityManager entityManager;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
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
    @Transactional(Transactional.TxType.REQUIRES_NEW)
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

    @Transactional(Transactional.TxType.REQUIRES_NEW)
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
