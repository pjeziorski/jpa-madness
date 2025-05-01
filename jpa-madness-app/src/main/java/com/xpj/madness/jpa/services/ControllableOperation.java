package com.xpj.madness.jpa.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@RequiredArgsConstructor
public class ControllableOperation<R> {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private AwaitResult<?> currentAwait;
    private CountDownLatch operationResultLock = new CountDownLatch(1);
    private R operationResult;
    private Throwable operationException;

    private final Function<ControllableOperation, R> operation;


    public ControllableOperation<R> start() {
        executor.submit(() -> {
            try {
                operationResult = operation.apply(this);
            }
            catch (Throwable e) {
                operationException = e;
            }
            finally {
                operationResultLock.countDown();
            }
        });
        awaitOperationAction();

        return this;
    }

    // use only inside operation
    public <T> T pauseBefore(Callable<T> subOperation) {
        AwaitResult<T> await = new AwaitResult<>(subOperation);
        currentAwait = await;
        return await.pause();
    }

    public boolean isPaused() {
        return currentAwait != null;
    }

    public Object resume() {
        AwaitResult await = currentAwait;
        currentAwait = null;
        Object result = await.resume();

        awaitOperationAction();

        return result;
    }

    @SneakyThrows
    public R complete() {
        while (isPaused()) {
            resume();
        }
        if (operationException != null) {
            throw operationException;
        }

        return operationResult;
    }

    private void awaitOperationAction() {
        for (int i = 0; i < 20; i++) {
            try {
                Thread.currentThread().sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (currentAwait != null || operationResultLock.getCount() == 0) {
                return;
            }
        }
        throw new RuntimeException("Timeout on waiting for operation action");
    }

    class AwaitResult<SubOperationResult> {

        private CountDownLatch subOperationLock = new CountDownLatch(1);
        private CountDownLatch resultLock = new CountDownLatch(1);
        private Callable<SubOperationResult> subOperation;
        private boolean shouldThrowRuntimeException = false;
        private boolean shouldThrowException = false;

        private SubOperationResult result;

        public AwaitResult(Callable<SubOperationResult> subOperation) {
            this.subOperation = subOperation;
        }

        @SneakyThrows
        public SubOperationResult pause() {
            subOperationLock.await();

            try {
                if (shouldThrowRuntimeException) {
                    throw new RuntimeException();
                }
                if (shouldThrowException) {
                    throw new Exception("Non Runtime Exception");
                }
                result = subOperation.call();

                return result;
            }
            catch (Throwable e) {
                System.err.println(e);
                throw e;
            }
            finally {
                resultLock.countDown();
            }
        }

        @SneakyThrows
        public Object resume() {
            subOperationLock.countDown();
            resultLock.await();
            return result;
        }

    }
}
