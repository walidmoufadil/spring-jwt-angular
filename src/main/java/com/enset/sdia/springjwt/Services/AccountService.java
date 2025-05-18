package com.enset.sdia.springjwt.Services;

import com.enset.sdia.springjwt.entities.Account;
import com.enset.sdia.springjwt.entities.Role;

public interface AccountService {
    Account addNewAccount(String username, String password, String roleName);
    void addRoleToAccount(String username, String roleName);
    Account loadAccountByUsername(String username);
    Role addNewRole(String roleName, String description);
    void changePassword(String username, String oldPassword, String newPassword) throws RuntimeException;
}
