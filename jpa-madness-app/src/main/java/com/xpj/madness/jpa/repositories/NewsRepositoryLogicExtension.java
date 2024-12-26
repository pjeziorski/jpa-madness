package com.xpj.madness.jpa.repositories;

import java.util.concurrent.atomic.AtomicBoolean;

public interface NewsRepositoryLogicExtension {

    AtomicBoolean shouldThrowRuntimeException = new AtomicBoolean(false);

    void transactionWithSave();

    void transactionWithSaveAndFlush();

    void privateTransactionWithSaveAndFlush();
}
