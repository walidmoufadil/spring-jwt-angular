package com.enset.sdia.springjwt.Services;

import com.enset.sdia.springjwt.Repositories.AccountRepository;
import com.enset.sdia.springjwt.Repositories.RoleRepository;
import com.enset.sdia.springjwt.entities.Account;
import com.enset.sdia.springjwt.entities.Role;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private AccountRepository accountRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public Account addNewAccount(String username, String password, String roleName) {
        log.info("Creating new account for username: {}", username);

        // Vérifier si le compte existe déjà
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Account already exists with username: " + username);
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password)); // Encoder le mot de passe ici
        account.setRoles(new ArrayList<>());
        Account savedAccount = accountRepository.save(account);

        addRoleToAccount(username, roleName);
        log.info("Account created successfully with ID: {} and role: {}", savedAccount.getId(), roleName);
        return savedAccount;
    }

    @Override
    public void addRoleToAccount(String username, String roleName) {
        log.info("Adding role " + roleName + " to account " + username);
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Role role = roleRepository.findByRoleName(roleName);
        if(role == null) {
            role = addNewRole(roleName, "Default description for " + roleName);
            log.info("Created new role: " + roleName);
        }
        account.getRoles().add(role);
        log.info("Role " + roleName + " added successfully to account " + username);
    }

    @Override
    public Account loadAccountByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Override
    public Role addNewRole(String roleName, String description) {
        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);
        return roleRepository.save(role);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) throws RuntimeException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Vérifier si l'ancien mot de passe est correct
        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new RuntimeException("Incorrect old password");
        }

        // Mettre à jour avec le nouveau mot de passe
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
