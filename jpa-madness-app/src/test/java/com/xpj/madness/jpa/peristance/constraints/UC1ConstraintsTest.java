package com.xpj.madness.jpa.peristance.constraints;

import com.xpj.madness.jpa.peristance.constraints.entity.UC1ArticleEntity;
import com.xpj.madness.jpa.peristance.constraints.repository.UC1ArticleEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class UC1ConstraintsTest {

    @Autowired
    private UC1ArticleEntityRepository articleEntityRepository;

    @Test
    public void shouldValidate_EntityConstraints_onSave() {
        // given
        UC1ArticleEntity article = UC1ArticleEntity.builder().build();

        // validate entity constraints
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> articleEntityRepository.save(article));
    }

    /**
     * By default, DataJpaTest is wrapping each test in single transaction
     * so save operations aren't passed to database before commit
     */
    @Test
    public void shouldNotValidate_DatabaseConstraints_onSave() {
        // given
        UC1ArticleEntity article = UC1ArticleEntity.builder()
                .content("some content")
                .build();

        // when
        UC1ArticleEntity savedArticle = articleEntityRepository.save(article);

        // then
        assertThat(savedArticle.getId()).isNotNull();
        assertThat(savedArticle).isSameAs(article);

        UC1ArticleEntity returnedArticle = articleEntityRepository.findById(savedArticle.getId()).get();

        assertThat(returnedArticle).isSameAs(savedArticle);
    }

    @Test
    public void shouldValidate_EntityConstraints_onSaveAndFlush() {
        // given
        UC1ArticleEntity article = UC1ArticleEntity.builder()
                .content("some content")
                .build();

        // validate entity constraints
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> articleEntityRepository.saveAndFlush(article));
    }

    @Test
    public void shouldValidate_DatabaseConstraints_onSaveAndFlush() {
        // given
        UC1ArticleEntity article = UC1ArticleEntity.builder().build();

        // validate entity constraints
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> articleEntityRepository.saveAndFlush(article));
    }

    @Test
    public void shouldValidateConstraintsOnAllEntities_onSaveAndFlush() {
        // given
        UC1ArticleEntity invalidArticle = UC1ArticleEntity.builder()
                .content("some content")
                .build();

        UC1ArticleEntity validArticle = UC1ArticleEntity.builder()
                .title("some title")
                .content("some content")
                .build();

        // when, then
        assertDoesNotThrow(() -> articleEntityRepository.save(invalidArticle));

        // validate both entities
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> articleEntityRepository.saveAndFlush(validArticle));
    }

    @Test
    public void shouldSaveValidEntities() {
        // given
        UC1ArticleEntity article1 = UC1ArticleEntity.builder()
                .title("title 1")
                .content("some content")
                .build();

        UC1ArticleEntity article2 = UC1ArticleEntity.builder()
                .title("title 2")
                .content("some content")
                .build();

        // when
        article1 = articleEntityRepository.save(article1);
        article2 = articleEntityRepository.saveAndFlush(article2);

        // then
        List<UC1ArticleEntity> articles = articleEntityRepository.findAllById(List.of(article1.getId(), article2.getId()));

        assertThat(articles.size()).isEqualTo(2);
    }

}
