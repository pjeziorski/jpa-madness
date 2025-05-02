package com.xpj.madness.jpa.repositories;

import com.xpj.madness.jpa.entities.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BankAccount a")
    List<BankAccount> findAllForChange();

    @Modifying
    @Query(value = "UPDATE bank_account SET balance = balance + :money", nativeQuery = true)
    int addMoneyNatively(@Param("money") int money);

}
