package com.xpj.madness.jpa.chatgpt.repeatableread.service;

import com.xpj.madness.jpa.chatgpt.repeatableread.entity.Account;
import com.xpj.madness.jpa.chatgpt.repeatableread.repository.Account2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhantomService {
    @Autowired
    private Account2Repository account2Repository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void transaction1() throws InterruptedException {
        System.out.println("[T1] First read:");
        List<Account> firstRead = account2Repository.findByBalanceGreaterThan(150);
        firstRead.forEach(a -> System.out.println("[T1] " + a.getId()));

        Thread.sleep(5000); // daj T2 czas na wstawienie nowego konta

        System.out.println("[T1] Second read:");
        List<Account> secondRead = account2Repository.findByBalanceGreaterThan(150);
        secondRead.forEach(a -> System.out.println("[T1] " + a.getId()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transaction2() {
        Account newAccount = new Account();
        newAccount.setId(3);
        newAccount.setBalance(300);
        System.out.println("[T2] Inserting new account");
        account2Repository.save(newAccount);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void repeatableReadTest() throws InterruptedException {
        System.out.println("[T1] First read:");
        Account a1 = account2Repository.findById(2).orElseThrow();
        System.out.println("[T1] Balance: " + a1.getBalance());

        Thread.sleep(5000); // T2 modyfikuje balance

        System.out.println("[T1] Second read:");
        Account a2 = account2Repository.findById(2).orElseThrow();
        System.out.println("[T1] Balance: " + a2.getBalance());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void modifyBalanceInSeparateTransaction() {
        Account acc = account2Repository.findById(2).orElseThrow();
        System.out.println("[T2] Modifying balance to 999");
        acc.setBalance(999);
        account2Repository.save(acc);
    }
}
