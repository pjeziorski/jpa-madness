package com.xpj.jpamadness.repositories;

import com.xpj.jpamadness.entities.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsComplexLogicRepository extends JpaRepository<News, Long>, NewsComplexLogicExtension {

}
