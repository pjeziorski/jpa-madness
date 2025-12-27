package com.xpj.madness.jpa.legacy.repositories;

import com.xpj.madness.jpa.legacy.entities.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepositoryLogicRepository extends JpaRepository<News, Long>, NewsRepositoryLogicExtension {

}
