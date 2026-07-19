package org.shashanka.controller;

import org.shashanka.domain.CreateAccountRequest;
import org.shashanka.entity.AccountModel;
import org.shashanka.service.AccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountsController {

    private final AccountService accountService;

    public AccountsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public AccountModel createAccount(final @RequestBody CreateAccountRequest createAccountRequest) {
        return accountService.createAccount(createAccountRequest);
    }

    @GetMapping("/{id}")
    public AccountModel get(final @PathVariable Long id) {
        return accountService.getAccount(id);
    }
}
