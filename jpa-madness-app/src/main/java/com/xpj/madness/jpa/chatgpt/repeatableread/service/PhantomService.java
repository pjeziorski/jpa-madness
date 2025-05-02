package com.xpj.madness.jpa.chatgpt.repeatableread.service;

import com.xpj.madness.jpa.chatgpt.repeatableread.entity.Account;
import com.xpj.madness.jpa.chatgpt.repeatableread.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhantomService {
    @Autowired
    private AccountRepository accountRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void transaction1() throws InterruptedException {
        System.out.println("[T1] First read:");
        List<Account> firstRead = accountRepository.findByBalanceGreaterThan(150);
        firstRead.forEach(a -> System.out.println("[T1] " + a.getId()));

        Thread.sleep(5000); // daj T2 czas na wstawienie nowego konta

        System.out.println("[T1] Second read:");
        List<Account> secondRead = accountRepository.findByBalanceGreaterThan(150);
        secondRead.forEach(a -> System.out.println("[T1] " + a.getId()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transaction2() {
        Account newAccount = new Account();
        newAccount.setId(3);
        newAccount.setBalance(300);
        System.out.println("[T2] Inserting new account");
        accountRepository.save(newAccount);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void repeatableReadTest() throws InterruptedException {
        System.out.println("[T1] First read:");
        Account a1 = accountRepository.findById(2).orElseThrow();
        System.out.println("[T1] Balance: " + a1.getBalance());

        Thread.sleep(5000); // T2 modyfikuje balance

        System.out.println("[T1] Second read:");
        Account a2 = accountRepository.findById(2).orElseThrow();
        System.out.println("[T1] Balance: " + a2.getBalance());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void modifyBalanceInSeparateTransaction() {
        Account acc = accountRepository.findById(2).orElseThrow();
        System.out.println("[T2] Modifying balance to 999");
        acc.setBalance(999);
        accountRepository.save(acc);
    }
}
