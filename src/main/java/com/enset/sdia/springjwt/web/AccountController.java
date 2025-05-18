package com.enset.sdia.springjwt.web;

import com.enset.sdia.springjwt.Services.AccountService;
import com.enset.sdia.springjwt.entities.Account;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
@CrossOrigin("*")
public class AccountController {
    private AccountService accountService;

    @PostMapping("/admin")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public Account createAdminAccount(@RequestParam String username, @RequestParam String password) {
        return accountService.addNewAccount(username, password, "ADMIN");
    }

    @PostMapping("/roles/add")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public void addRoleToAccount(@RequestParam String username, @RequestParam String roleName) {
        accountService.addRoleToAccount(username, roleName);
    }
}
