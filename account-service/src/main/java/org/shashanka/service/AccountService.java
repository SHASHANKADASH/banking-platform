package org.shashanka.service;

import org.shashanka.exception.ResourceNotFoundException;
import org.shashanka.domain.CreateAccountRequest;
import org.shashanka.entity.AccountModel;
import org.shashanka.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountModel createAccount(final CreateAccountRequest createAccountRequest) {
        final AccountModel accountModel = AccountModel.builder()
                .id(createAccountRequest.getId())
                .balance(createAccountRequest.getBalance())
                .build();
        accountRepository.save(
                accountModel
        );
        return accountModel;
    }

    public AccountModel getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account Not found"));
    }
}
