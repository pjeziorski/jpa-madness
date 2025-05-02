package com.xpj.madness.jpa.chatgpt.repeatableread.repository;

import com.xpj.madness.jpa.chatgpt.repeatableread.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findByBalanceGreaterThan(int amount);
}