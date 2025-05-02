package com.xpj.madness.jpa.chatgpt.repeatableread.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Account {
    @Id
    private Integer id;
    private Integer balance;

    // Gettery i settery
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getBalance() { return balance; }
    public void setBalance(Integer balance) { this.balance = balance; }
}
