package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.BankAccount;
import com.xpj.madness.jpa.services.BankAccountService;
import com.xpj.madness.jpa.utils.ControllableOperation;
import com.xpj.madness.jpa.utils.ControllableOperationExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@DataJpaTest
@ComponentScan("com.xpj.madness.jpa.services")
@Transactional(propagation = Propagation.NOT_SUPPORTED) // see UnitTestsTransactionsTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // to use application db
public class IsolationLevelOnBankAccountTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankAccountService bankAccountService;

    private ControllableOperationExecutor controllableOperationExecutor = new ControllableOperationExecutor();

    @BeforeEach
    public void setUp() {
        bankAccountRepository.deleteAll();
    }

    @Test
    public void performTest_onAddMoney() {
        Isolation isolationLevel = Isolation.REPEATABLE_READ;

        BankAccount account = bankAccountRepository.saveAndFlush(BankAccount.builder()
                        .balance(1000)
                        .build());

        ControllableOperation<BankAccount> addMoney1 = bankAccountService.addMoney(isolationLevel, account.getId(), 200);
        ControllableOperation<BankAccount> addMoney2 = bankAccountService.addMoney(isolationLevel, account.getId(), 400);

        addMoney1.start();
        addMoney2.start();

        controllableOperationExecutor.completeAlternately(addMoney1, addMoney2);

        System.err.println("Add money1 result: " + addMoney1.complete().getBalance());
        System.err.println("Add money2 result: " + addMoney2.complete().getBalance());
        System.err.println("Database result: " + bankAccountRepository.findById(account.getId()).get());
    }

    @Test
    public void performTest_onAddMoneyToAllAccounts() {
        Isolation isolationLevel = Isolation.READ_COMMITTED;

        BankAccount account = bankAccountRepository.saveAndFlush(BankAccount.builder()
                .balance(1000)
                .build());

        ControllableOperation<List<BankAccount>> addMoneyToAll = bankAccountService.addMoneyToAllAccountsNatively(isolationLevel, 200);
        ControllableOperation<BankAccount> insertAccount = bankAccountService.insertAndFlush(isolationLevel, BankAccount.builder()
                .balance(1000)
                .build());

        addMoneyToAll.start();
        insertAccount.start();

        controllableOperationExecutor.completeAlternately(addMoneyToAll, insertAccount);

        addMoneyToAll.complete().forEach(System.err::println);

        System.err.println("====");
        List<BankAccount> allAccounts = bankAccountRepository.findAll();

        allAccounts.forEach(System.err::println);
    }



}
