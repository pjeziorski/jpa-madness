package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.News;
import com.xpj.madness.jpa.repositories.NewsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnforcedTransactionsNewsService {

    private final NewsRepository newsRepository;

    @Transactional
    public News save(News news) {
        return newsRepository.save(news);
    }

    @Transactional
    public News saveAndFlush(News news) {
        return newsRepository.saveAndFlush(news);
    }

    @Transactional
    public Optional<News> findById(Long id) {
        Optional<News> newsOpt = newsRepository.findById(id);

        if (newsOpt.isPresent()) {
            fetchDependingEntities(newsOpt.get());
        }
        return newsOpt;
    }

    private void fetchDependingEntities(News news) {
        news.toString();
    }

}
