package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.Article;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConstraintsTest {

    @Autowired
    ArticleRepository articleRepository;

    @BeforeAll
    public void beforeAll() {
        articleRepository.recreateTable();
    }

    @Test
    public void shouldValidateEntityConstraints_onSave() {
        // given
        Article article = Article.builder().build();

        // validate entity constraints
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> articleRepository.save(article));

        // BUT ignore table defined constraints
        Article article2 = Article.builder()
                .content("some content")
                .build();

        // when
        Article savedArticle = articleRepository.save(article2);

        // then
        assertThat(savedArticle.getId()).isNotNull();
        assertThat(savedArticle).isSameAs(article2);

        Article returnedArticle = articleRepository.findById(savedArticle.getId()).get();

        assertThat(returnedArticle).isSameAs(savedArticle);
    }

    @Test
    public void shouldValidateEntityConstraints_onSaveAndFlush() {
        // given
        Article article = Article.builder().build();

        // validate entity constraints
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> articleRepository.saveAndFlush(article));
    }

    @Test
    public void shouldValidateTableConstraints_onSaveAndFlush() {
        // given
        Article article = Article.builder()
                .content("some content")
                .build();

        // validate entity constraints
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> articleRepository.saveAndFlush(article));
    }

    @Test
    public void shouldSaveValidEntity_onSaveAndFlush() {
        // given
        Article article = Article.builder()
                .title("some title")
                .content("some content")
                .build();

        // when
        Article savedArticle = articleRepository.saveAndFlush(article);

        // then
        assertThat(savedArticle.getId()).isNotNull();
        assertThat(savedArticle).isSameAs(article);

        Article returnedArticle = articleRepository.findById(savedArticle.getId()).get();

        assertThat(returnedArticle).isSameAs(savedArticle);
    }

    @Test
    public void shouldValidateConstraintsOnAllEntities_onSaveAndFlush() {
        // given
        Article invalidArticle = Article.builder()
                .content("some content")
                .build();

        Article validArticle = Article.builder()
                .content("some content")
                .build();

        // when, then
        assertDoesNotThrow(() -> articleRepository.save(invalidArticle));

        // validate both entities
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> articleRepository.saveAndFlush(validArticle));
    }
}
