package com.xpj.madness.jpa.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ControllableOperationExecutor {

    private static final int MAX_AWAITING_COUNT = 5;

    public void completeAlternately(ControllableOperation<?>... operations) {
        int currentOperationId = 0;
        int allOperationsAwaitingCount = 0;

        List<OperationExecution> executions = Arrays.stream(operations)
                .map(operation -> new OperationExecution(operation))
                .collect(Collectors.toList());

        while (!areAllComplete(executions)) {
            OperationExecution execution = executions.get(currentOperationId);

            execution.execute();
            currentOperationId = (currentOperationId + 1) % executions.size();

            if (currentOperationId == 0) {
                if (areAllAwaiting(executions)) {
                    allOperationsAwaitingCount++;
                }
                else {
                    allOperationsAwaitingCount = 0;
                }
                if (allOperationsAwaitingCount == MAX_AWAITING_COUNT) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (allOperationsAwaitingCount > MAX_AWAITING_COUNT) {
                    throw new RuntimeException("Possible deadlock: All operations are awaiting.");
                }
            }
        }
    }

    private boolean areAllComplete(List<OperationExecution> executions) {
        return executions.stream().allMatch(OperationExecution::isComplete);
    }

    private boolean areAllAwaiting(List<OperationExecution> executions) {
        return executions.stream().allMatch(OperationExecution::isAwaiting);
    }

    class OperationExecution {

        private final ControllableOperation<?> operation;
        private Future<?> futureResult;

        public OperationExecution(ControllableOperation<?> operation) {
            this.operation = operation;
        }

        public void execute() {
            if (futureResult == null || futureResult.isDone()) {
                if (operation.isPaused()) {
                    futureResult = operation.resumeAsync();
                }
            }
        }

        public boolean isAwaiting() {
            return futureResult != null && !futureResult.isDone();
        }

        public boolean isComplete() {
            return !isAwaiting() && !operation.isPaused();
        }

    }

}
