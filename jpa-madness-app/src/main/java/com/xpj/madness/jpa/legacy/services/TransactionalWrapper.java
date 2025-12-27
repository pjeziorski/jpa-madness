package com.xpj.madness.jpa.legacy.services;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
public class TransactionalWrapper {

    private final TransactionalWrapper transactionalWrapper;

    public TransactionalWrapper(@Lazy TransactionalWrapper transactionalWrapper) {
        this.transactionalWrapper = transactionalWrapper;
    }

    public <T> T wrap3(Isolation isolationLevel, Supplier<T> operation) {
        return operation.get();
    }

    public <T> T wrap(Isolation isolationLevel, Supplier<T> operation) {
        switch (isolationLevel) {
            case READ_UNCOMMITTED:
                return transactionalWrapper.readUncommitted(operation);
            case READ_COMMITTED:
                return transactionalWrapper.readCommitted(operation);
            case REPEATABLE_READ:
                return transactionalWrapper.repeatableRead(operation);
            case SERIALIZABLE:
                return transactionalWrapper.serializable(operation);
            default:
                throw new UnsupportedOperationException("Unsupported level: " + isolationLevel);
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public <T> T readUncommitted(Supplier<T> operation) {
        return operation.get();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public <T> T readCommitted(Supplier<T> operation) {
        return operation.get();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public <T> T repeatableRead(Supplier<T> operation) {
        return operation.get();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public <T> T serializable(Supplier<T> operation) {
        return operation.get();
    }

}
