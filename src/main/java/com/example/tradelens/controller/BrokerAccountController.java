
package com.example.tradelens.controller;

import com.example.tradelens.dto.BrokerAccountResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.User;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/api/broker-accounts")
@RequiredArgsConstructor
public class BrokerAccountController {

    private final BrokerAccountRepository brokerAccountRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<BrokerAccountResponse> getMyBrokerAccounts(
            Authentication authentication) {

        return brokerAccountRepository
                .findByUser_Username(authentication.getName())
                .stream()
                .map(account -> new BrokerAccountResponse(
                        account.getId(),
                        account.getBrokerName(),
                        account.getAccountName()
                ))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BrokerAccountResponse createBrokerAccount(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String brokerName = request.get("brokerName");
        String accountName = request.get("accountName");

        if (brokerName == null || brokerName.isBlank()
                || accountName == null || accountName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Broker name and account name are required"
            );
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        BrokerAccount account = new BrokerAccount();
        account.setBrokerName(brokerName.trim());
        account.setAccountName(accountName.trim());
        account.setUser(user);

        BrokerAccount savedAccount = brokerAccountRepository.save(account);

        return new BrokerAccountResponse(
                savedAccount.getId(),
                savedAccount.getBrokerName(),
                savedAccount.getAccountName()
        );

    }
}
