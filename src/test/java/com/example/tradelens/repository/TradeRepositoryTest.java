package com.example.tradelens.repository;


import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.User;
import com.example.tradelens.entity.enums.InstrumentType;
import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;




@SpringBootTest
public class TradeRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void shouldSaveTrade() {

        User user = new User();
        user.setUsername("trade-test");
        user.setEmail("trade@test.com");
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

        Trade trade = new Trade();
        trade.setInstrument(instrument);
        trade.setBrokerAccount(brokerAccount);
        trade.setQuantity(new BigDecimal("100"));
        trade.setEntryPrice(new BigDecimal("1400.00"));
        trade.setExitPrice(new BigDecimal("1430.00"));
        trade.setEntryTime(LocalDateTime.now().minusMinutes(30));
        trade.setExitTime(LocalDateTime.now());
        trade.setProfitLoss(new BigDecimal("3000.00"));

        Trade savedTrade = tradeRepository.save(trade);

        System.out.println("Trade ID: " + savedTrade.getId());

    }

}
