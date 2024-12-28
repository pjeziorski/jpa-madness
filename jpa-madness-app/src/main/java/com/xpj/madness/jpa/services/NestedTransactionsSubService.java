package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.News;
import com.xpj.madness.jpa.repositories.NewsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NestedTransactionsSubService {

    private final NewsRepository newsRepository;

    @Setter
    private boolean shouldThrowRuntimeException = false;

    @Setter
    private boolean shouldThrowException = false;

    @Transactional
    public void transactionWithSave() {
        News news1 = News.builder()
                .title("a")
                .build();

        newsRepository.save(news1);

        log.info("nested news1 saved");

        if (shouldThrowRuntimeException) {
            throw new RuntimeException("Breaking save news2");
        }
        if (shouldThrowException) {
            throwException();
        }
        News news2 = News.builder()
                .title("b")
                .build();

        newsRepository.save(news2);

        log.info("nested news2 saved");
    }

    @Transactional
    public void transactionWithSaveAndFlush() {
        News news1 = News.builder()
                .title("a")
                .build();

        newsRepository.saveAndFlush(news1);

        log.info("nested news1 savedAndFlushed");

        if (shouldThrowRuntimeException) {
            throw new RuntimeException("Breaking save news2");
        }
        if (shouldThrowException) {
            throwException();
        }
        News news2 = News.builder()
                .title("b")
                .build();

        newsRepository.saveAndFlush(news2);

        log.info("nested news2 savedAndFlushed");
    }

    @SneakyThrows
    private void throwException() {
        throw new Exception("Non Runtime Exception");
    }
}
