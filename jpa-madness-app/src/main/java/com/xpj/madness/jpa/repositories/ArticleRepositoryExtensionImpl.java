package com.xpj.madness.jpa.repositories;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ArticleRepositoryExtensionImpl implements ArticleRepositoryExtension {

    private final EntityManager entityManager;

    /**
     * It is to enforce situation of incorrect definition of the Entity (null values)
     */
    @Override
    @Transactional
    public void recreateTable() {
        String recreateArticleDdl = "DROP TABLE IF EXISTS article;\n" +
                "\n" +
                "CREATE TABLE article (\n" +
                "    id VARCHAR(255) NOT NULL PRIMARY KEY,\n" +
                "    title VARCHAR(255) NOT NULL,\n" +
                "    content TEXT NOT NULL\n" +
                ");";

        log.info("Recreate article table");

        entityManager.createNativeQuery(recreateArticleDdl).executeUpdate();

        log.info("article table recreated");
    }

}
