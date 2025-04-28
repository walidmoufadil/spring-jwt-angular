package com.enset.sdia.springjwt.Repositories;


import com.enset.sdia.springjwt.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount,String> {
}