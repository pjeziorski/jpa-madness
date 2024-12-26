package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.News;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;

public class NewsRepositoryLogicExtensionImpl implements NewsRepositoryLogicExtension {

    private final NewsRepositoryLogicRepository repository;

    public NewsRepositoryLogicExtensionImpl(@Lazy NewsRepositoryLogicRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void transactionWithSave() {
        News news1 = News.builder()
                .title("a")
                .build();

        repository.save(news1);

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        repository.save(news2);
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void transactionWithSaveAndFlush() {
        News news1 = News.builder()
                .title("a")
                .build();

        repository.saveAndFlush(news1);

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        repository.saveAndFlush(news2);
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

        repository.saveAndFlush(news1);

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        repository.saveAndFlush(news2);
    }
}
