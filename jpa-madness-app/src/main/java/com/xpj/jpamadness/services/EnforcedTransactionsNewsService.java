package com.xpj.jpamadness.services;

import com.xpj.jpamadness.entities.News;
import com.xpj.jpamadness.repositories.NewsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnforcedTransactionsNewsService {

    private final NewsRepository newsRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public News save(News news) {
        return newsRepository.save(news);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public News saveAndFlush(News news) {
        return newsRepository.saveAndFlush(news);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
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
