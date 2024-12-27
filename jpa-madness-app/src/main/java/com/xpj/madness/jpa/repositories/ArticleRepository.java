package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String>, ArticleRepositoryExtension {
}
