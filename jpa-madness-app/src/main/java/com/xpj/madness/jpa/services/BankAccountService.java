package com.xpj.madness.jpa.services;

import com.xpj.madness.jpa.entities.BankAccount;
import com.xpj.madness.jpa.repositories.BankAccountRepository;
import com.xpj.madness.jpa.utils.ControllableOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionalWrapper transactionalWrapper;

    public ControllableOperation<BankAccount> addMoney(Isolation isolationLevel, String bankAccountId, int money) {
        return new ControllableOperation<>(
                "addMoney",
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> {
                            BankAccount account = ctrl.pauseBefore("findById",
                                    () -> bankAccountRepository.findById(bankAccountId).get());
                            account.setBalance(account.getBalance() + money);

                            BankAccount savedAccount = ctrl.pauseBefore("saveAndFlush",
                                    () -> bankAccountRepository.saveAndFlush(account));
                            return ctrl.pauseBefore("commit", () -> savedAccount);
                        })
        );
    }

    public ControllableOperation<List<BankAccount>> addMoneyToAllAccounts(Isolation isolationLevel, int money) {
        return new ControllableOperation<>(
                "addMoneyToAllAccounts",
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> {
                            ctrl.pauseBefore("startTransaction",
                                    () -> bankAccountRepository.findAllForChange());

                            List<BankAccount> accounts = ctrl.pauseBefore("findAll",
                                    () -> bankAccountRepository.findAllForChange());

                            accounts.forEach(account -> account.setBalance(account.getBalance() + money));

                            List<BankAccount> savedAccounts = ctrl.pauseBefore("saveAllAndFlush",
                                    () -> bankAccountRepository.saveAllAndFlush(accounts));
                            return ctrl.pauseBefore("commit", () -> savedAccounts);
                        })
        );
    }

    public ControllableOperation<List<BankAccount>> addMoneyToAllAccountsNatively(Isolation isolationLevel, int money) {
        return new ControllableOperation<>(
                "addMoneyToAllAccountsNatively",
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> {
                            ctrl.pauseBefore("addMoneyNatively",
                                    () -> bankAccountRepository.addMoneyNatively(money));

                            List<BankAccount> savedAccounts = ctrl.pauseBefore("findAll",
                                    () -> bankAccountRepository.findAll());

                            return ctrl.pauseBefore("commit", () -> savedAccounts);
                        })
        );
    }

    public ControllableOperation<BankAccount> insertAndFlush(Isolation isolationLevel, BankAccount bankAccount) {
        return saveAndFlush("insertAndFlush", isolationLevel, bankAccount);
    }

    private ControllableOperation<BankAccount> saveAndFlush(String saveName, Isolation isolationLevel, BankAccount bankAccount) {
        return new ControllableOperation<>(
                saveName + "-" + isolationLevel,
                (ctrl) -> transactionalWrapper.wrap(isolationLevel,
                        () -> {
                            BankAccount savedBankAccount = ctrl.pauseBefore("saveAndFlush", () ->
                                    bankAccountRepository.saveAndFlush(bankAccount));

                            return ctrl.pauseBefore("commit", () -> savedBankAccount);
                        }));
    }
}
