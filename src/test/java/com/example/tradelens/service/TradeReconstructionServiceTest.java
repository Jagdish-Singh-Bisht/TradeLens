package com.example.tradelens.service;



import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.Execution;
import com.example.tradelens.entity.User;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.enums.ExecutionSide;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.repository.TradeRepository;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.ExecutionRepository;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;




@SpringBootTest
@Transactional
public class TradeReconstructionServiceTest {

    @Autowired
    private TradeReconstructionService tradeReconstructionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void shouldReconstructTrade() {

        User user = new User();
        user.setUsername("reconstruction-test");
        user.setEmail("reconstruction@test.com");
        user.setPassword("test123");

        user =  userRepository.save(user);

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

        Execution buyExecution = new Execution();
        buyExecution.setBrokerAccount(brokerAccount);
        buyExecution.setInstrument(instrument);
        buyExecution.setOrderId("BUY-TEST-001");
        buyExecution.setSide(ExecutionSide.BUY);
        buyExecution.setQuantity(new BigDecimal("100"));
        buyExecution.setPrice(new BigDecimal("1400.00"));
        buyExecution.setExecutedAt(LocalDateTime.now().minusMinutes(20));

        buyExecution = executionRepository.save(buyExecution);

        Execution sellExecution1 = new Execution();
        sellExecution1.setBrokerAccount(brokerAccount);
        sellExecution1.setInstrument(instrument);
        sellExecution1.setOrderId("SELL-TEST-001");
        sellExecution1.setSide(ExecutionSide.SELL);
        sellExecution1.setQuantity(new BigDecimal("40"));
        sellExecution1.setPrice(new BigDecimal("1420.00"));
        sellExecution1.setExecutedAt(LocalDateTime.now().minusMinutes(10));

        sellExecution1 = executionRepository.save(sellExecution1);

        Execution sellExecution2 = new Execution();
        sellExecution2.setBrokerAccount(brokerAccount);
        sellExecution2.setInstrument(instrument);
        sellExecution2.setOrderId("SELL-TEST-002");
        sellExecution2.setSide(ExecutionSide.SELL);
        sellExecution2.setQuantity(new BigDecimal("60"));
        sellExecution2.setPrice(new BigDecimal("1430.00"));
        sellExecution2.setExecutedAt(LocalDateTime.now());

        sellExecution2 = executionRepository.save(sellExecution2);

        tradeReconstructionService.processExecution(buyExecution);
        tradeReconstructionService.processExecution(sellExecution1);
        tradeReconstructionService.processExecution(sellExecution2);

        List<Trade> trades = tradeRepository.findByBrokerAccount(brokerAccount);

        assertEquals(2, trades.size());

        Trade firstTrade = trades.get(0);
        Trade secondTrade = trades.get(1);

        assertEquals(new BigDecimal("1400.00"), firstTrade.getEntryPrice());
        assertEquals(new BigDecimal("1420.00"), firstTrade.getExitPrice());
        assertEquals(new BigDecimal("800.00"), firstTrade.getProfitLoss());

        assertEquals(new BigDecimal("1400.00"), secondTrade.getEntryPrice());
        assertEquals(new BigDecimal("1430.00"), secondTrade.getExitPrice());
        assertEquals(new BigDecimal("1800.00"), secondTrade.getProfitLoss());

    }

    @Test
    void shouldKeepPositionsIsolatedByBrokerAccount() {

        // Account 1
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");
        user1.setPassword("test123");
        user1 = userRepository.save(user1);

        BrokerAccount account1 = new BrokerAccount();
        account1.setBrokerName("Broker A");
        account1.setAccountName("Account A");
        account1.setUser(user1);
        account1 = brokerAccountRepository.save(account1);

        // Account 2
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@test.com");
        user2.setPassword("test123");
        user2 = userRepository.save(user2);

        BrokerAccount account2 = new BrokerAccount();
        account2.setBrokerName("Broker B");
        account2.setAccountName("Account B");
        account2.setUser(user2);
        account2 = brokerAccountRepository.save(account2);

        // Same instrument for both accounts
        Instrument instrument = new Instrument();
        instrument.setSymbol("RELIANCE");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);
        instrument = instrumentRepository.save(instrument);

        // Account 1 buys RELIANCE
        Execution account1Buy = new Execution();
        account1Buy.setBrokerAccount(account1);
        account1Buy.setInstrument(instrument);
        account1Buy.setOrderId("A1-BUY");
        account1Buy.setSide(ExecutionSide.BUY);
        account1Buy.setQuantity(new BigDecimal("100"));
        account1Buy.setPrice(new BigDecimal("1400.00"));
        account1Buy.setExecutedAt(LocalDateTime.now().minusMinutes(10));

        // Account 2 tries to sell RELIANCE
        Execution account2Sell = new Execution();
        account2Sell.setBrokerAccount(account2);
        account2Sell.setInstrument(instrument);
        account2Sell.setOrderId("A2-SELL");
        account2Sell.setSide(ExecutionSide.SELL);
        account2Sell.setQuantity(new BigDecimal("100"));
        account2Sell.setPrice(new BigDecimal("1500.00"));
        account2Sell.setExecutedAt(LocalDateTime.now());

        tradeReconstructionService.processExecution(account1Buy);

        assertThrows(
                IllegalStateException.class,
                () -> tradeReconstructionService.processExecution(account2Sell)
        );
    }

    @Test
    void shouldKeepPositionsIsolatedByInstrument() {

        User user = new User();
        user.setUsername("instrument-test");
        user.setEmail("instrument@test.com");
        user.setPassword("test123");
        user = userRepository.save(user);

        BrokerAccount account = new BrokerAccount();
        account.setBrokerName("Test Broker");
        account.setAccountName("Test Account");
        account.setUser(user);
        account = brokerAccountRepository.save(account);

        Instrument reliance = new Instrument();
        reliance.setSymbol("RELIANCE");
        reliance.setExchange("NSE");
        reliance.setType(InstrumentType.EQUITY);
        reliance = instrumentRepository.save(reliance);

        Instrument tcs = new Instrument();
        tcs.setSymbol("TCS");
        tcs.setExchange("NSE");
        tcs.setType(InstrumentType.EQUITY);
        tcs = instrumentRepository.save(tcs);

        Execution relianceBuy = new Execution();
        relianceBuy.setBrokerAccount(account);
        relianceBuy.setInstrument(reliance);
        relianceBuy.setOrderId("RELIANCE-BUY");
        relianceBuy.setSide(ExecutionSide.BUY);
        relianceBuy.setQuantity(new BigDecimal("100"));
        relianceBuy.setPrice(new BigDecimal("1400.00"));
        relianceBuy.setExecutedAt(LocalDateTime.now().minusMinutes(10));

        Execution tcsSell = new Execution();
        tcsSell.setBrokerAccount(account);
        tcsSell.setInstrument(tcs);
        tcsSell.setOrderId("TCS-SELL");
        tcsSell.setSide(ExecutionSide.SELL);
        tcsSell.setQuantity(new BigDecimal("100"));
        tcsSell.setPrice(new BigDecimal("4000.00"));
        tcsSell.setExecutedAt(LocalDateTime.now());

        tradeReconstructionService.processExecution(relianceBuy);

        assertThrows(
                IllegalStateException.class,
                () -> tradeReconstructionService.processExecution(tcsSell)
        );
    }

}
