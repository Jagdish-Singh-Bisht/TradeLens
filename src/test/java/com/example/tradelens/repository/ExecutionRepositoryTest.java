package com.example.tradelens.repository;



import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.User;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.Execution;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.entity.enums.ExecutionSide;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@SpringBootTest
public class ExecutionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ExecutionRepository executionRepository;


    @Test
    void shouldSaveExecution() {

        User user = new User();
        user.setUsername("execution-test");
        user.setEmail("execution@test.com");
        user.setPassword("test123");

        user = userRepository.save(user);

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setBrokerName("Test Broker");
        brokerAccount.setAccountName("Test Account");
        brokerAccount.setUser(user);

        brokerAccount = brokerAccountRepository.save(brokerAccount);

        Instrument instrument = new Instrument();
        instrument.setSymbol("RELIANCE");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        instrument = instrumentRepository.save(instrument);

        Execution execution = new Execution();
        execution.setBrokerAccount(brokerAccount);
        execution.setInstrument(instrument);
        execution.setOrderId("TEST-ORDER-001");
        execution.setSide(ExecutionSide.BUY);
        execution.setQuantity(new BigDecimal("30"));
        execution.setPrice(new BigDecimal("1400.50"));
        execution.setExecutedAt(LocalDateTime.now());

        Execution savedExecution = executionRepository.save(execution);

        System.out.println("Execution ID: " + savedExecution.getId());

    }


}
