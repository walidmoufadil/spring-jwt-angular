package com.enset.sdia.springjwt.web;

import com.enset.sdia.springjwt.Services.BankAccountService;
import com.enset.sdia.springjwt.dtos.*;
import com.enset.sdia.springjwt.exceptions.BalanceNotSufficientException;
import com.enset.sdia.springjwt.exceptions.BankAccountNotFoundException;
import com.enset.sdia.springjwt.exceptions.CustomerNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
public class BankAccountRestAPI {
    private BankAccountService bankAccountService;

    public BankAccountRestAPI(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }


    @GetMapping("/accounts/{accountId}")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        return bankAccountService.getBankAccount(accountId);
    }

    @GetMapping("/accounts")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public List<BankAccountDTO> listAccounts(){
        return bankAccountService.bankAccountList();
    }

    @GetMapping("/accounts/{accountId}/operations")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public List<AccountOperationDTO> getHistory(@PathVariable String accountId){
        return bankAccountService.accountHistory(accountId);
    }

    @GetMapping("/accounts/{accountId}/pageOperations")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public AccountHistoryDTO getAccountHistory(
            @PathVariable String accountId,
            @RequestParam(name="page",defaultValue = "0") int page,
            @RequestParam(name="size",defaultValue = "5")int size) throws BankAccountNotFoundException {
        return bankAccountService.getAccountHistory(accountId,page,size);
    }

    @GetMapping("/accounts/customer/{customerId}")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public List<BankAccountDTO> getCustomerAccounts(@PathVariable Long customerId) throws CustomerNotFoundException {
        return bankAccountService.getCustomerAccounts(customerId);
    }

    @PostMapping("/accounts/debit")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.debit(debitDTO.getAccountId(),debitDTO.getAmount(),debitDTO.getDescription());
        return debitDTO;
    }
    @PostMapping("/accounts/credit")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException {
        this.bankAccountService.credit(creditDTO.getAccountId(),creditDTO.getAmount(),creditDTO.getDescription());
        return creditDTO;
    }
    @PostMapping("/accounts/transfer")
    //@PreAuthorize("hasAuthority('SCOPE_ROLE_CUSTOMER')")
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.transfer(
                transferRequestDTO.getAccountSource(),
                transferRequestDTO.getAccountDestination(),
                transferRequestDTO.getAmount());
    }
}
