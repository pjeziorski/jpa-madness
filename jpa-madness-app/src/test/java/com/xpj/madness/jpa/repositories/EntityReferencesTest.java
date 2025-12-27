package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.TestUtils;
import com.xpj.madness.jpa.legacy.entities.News;
import com.xpj.madness.jpa.legacy.repositories.NewsRepository;
import com.xpj.madness.jpa.legacy.services.EnforcedTransactionsNewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Deprecated
@DataJpaTest
@ActiveProfiles("legacy")
@ComponentScan("com.xpj.madness.jpa.legacy.services")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
class EntityReferencesTest {

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    EnforcedTransactionsNewsService enforcedTransactionsNewsService;

    @Test
    public void shouldReturn_sameReference_whenCallingFindById() {
        // given
        News news = News.builder()
                .title("Awesome")
                .build();

        // when
        News savedNews = newsRepository.save(news);

        // then
        assertThat(savedNews.getId()).isNotNull();
        assertThat(savedNews).isSameAs(news);

        News returnedNews = newsRepository.findById(savedNews.getId()).get();

        assertThat(returnedNews).isSameAs(savedNews);
    }

    @Test
    public void shouldReturn_sameReference_whenCallingFindById_andSaveAndFlush() {
        // given
        News news = News.builder()
                .title("Awesome")
                .build();

        // when
        News savedNews = newsRepository.saveAndFlush(news);

        // then
        assertThat(savedNews.getId()).isNotNull();
        assertThat(savedNews).isSameAs(news);

        News returnedNews = newsRepository.findById(savedNews.getId()).get();

        assertThat(returnedNews).isSameAs(savedNews);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
    public void shouldReturn_differentReference_whenCallingFindById_onDifferentTransactions() {
        // given
        News news = News.builder()
                .title("Awesome")
                .build();

        // when
        News savedNews = enforcedTransactionsNewsService.save(news);

        // then
        assertThat(savedNews.getId()).isNotNull();
        assertThat(savedNews).isSameAs(news);

        News returnedNews = enforcedTransactionsNewsService.findById(savedNews.getId()).get();

        TestUtils.printHash("savedNews", savedNews);
        TestUtils.printHash("returnedNews", returnedNews);

        assertThat(returnedNews).isNotSameAs(savedNews);

        assertThat(savedNews.getComments()).isNull();
        assertThat(returnedNews.getComments()).isNotNull();

        assertThat(returnedNews)
                .usingRecursiveComparison()
                .ignoringFields("comments")
                .isEqualTo(savedNews);
    }


}