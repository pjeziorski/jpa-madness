package com.xpj.madness.jpa.legacy.repositories;

import com.xpj.madness.jpa.legacy.entities.News;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.concurrent.atomic.AtomicBoolean;

@Repository
public interface NewsRepositoryWithDefaultMethod extends JpaRepository<News, Long> {

    AtomicBoolean shouldThrowRuntimeException = new AtomicBoolean(false);

    @Transactional
    default void transactionWithSave() {
        News news1 = News.builder()
                .title("a")
                .build();

        save(news1);

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        save(news2);
    }

    @Transactional
    default void transactionWithSaveAndFlush() {
        News news1 = News.builder()
                .title("a")
                .build();

        saveAndFlush(news1);

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        saveAndFlush(news2);
    }

    default void transactionalSaveAndFlushInNonTransactional() {
        transactionWithSaveAndFlush();
    }

    default void nonTransactionalWithSave() {
        News news1 = News.builder()
                .title("a")
                .build();

        save(news1);

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        save(news2);
    }

    default void nonTransactionalWithSaveAndFlush() {
        News news1 = News.builder()
                .title("a")
                .build();

        saveAndFlush(news1);

        if (shouldThrowRuntimeException.get()) {
            throw new RuntimeException("Breaking save news2");
        }
        News news2 = News.builder()
                .title("b")
                .build();

        saveAndFlush(news2);
    }

}
