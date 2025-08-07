package com.xpj.madness.jpa.chatgpt.repeatableread;

import com.xpj.madness.jpa.chatgpt.repeatableread.entity.Account;
import com.xpj.madness.jpa.chatgpt.repeatableread.repository.Account2Repository;
import com.xpj.madness.jpa.chatgpt.repeatableread.service.PhantomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class DemoApplication implements CommandLineRunner {
    @Autowired
    private PhantomService phantomService;

    @Autowired
    private Account2Repository account2Repository;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // dane początkowe
        Account a1 = new Account(); a1.setId(1); a1.setBalance(100);
        Account a2 = new Account(); a2.setId(2); a2.setBalance(200);
        account2Repository.deleteAll();
        account2Repository.save(a1);
        account2Repository.save(a2);

        Thread t1 = new Thread(() -> {
            try {
                phantomService.repeatableReadTest();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(2000);
                phantomService.modifyBalanceInSeparateTransaction();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
