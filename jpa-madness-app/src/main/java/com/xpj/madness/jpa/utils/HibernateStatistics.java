package com.xpj.madness.jpa.utils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HibernateStatistics {

    private final SessionFactory sessionFactory;

    public long getQueryCount() {
        return sessionFactory.getStatistics().getPrepareStatementCount();
    }

    public void logSummary() {
        sessionFactory.getStatistics().logSummary();
    }
}
