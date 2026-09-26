package com.example.tradelens.controller;


import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.User;
//import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.TradeRepository;
import com.example.tradelens.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TradeAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void shouldReturnTradeAnalytics() throws Exception {

        User user = new User();
        user.setUsername("analytics-user");
        user.setEmail("analytics@test.com");
        user.setPassword("password");

        user = userRepository.save(user);

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setBrokerName("Test Broker");
        brokerAccount.setAccountName("Test Account");
        brokerAccount.setUser(user);

        brokerAccount = brokerAccountRepository.save(brokerAccount);

        Trade trade1 = createTrade(brokerAccount, "1000");
        Trade trade2 = createTrade(brokerAccount, "500");
        Trade trade3 = createTrade(brokerAccount, "-400");
        Trade trade4 = createTrade(brokerAccount, "300");
        Trade trade5 = createTrade(brokerAccount, "-200");

        tradeRepository.save(trade1);
        tradeRepository.save(trade2);
        tradeRepository.save(trade3);
        tradeRepository.save(trade4);
        tradeRepository.save(trade5);

        mockMvc.perform(
                get("/api/broker-accounts/{brokerAccountId}/analytics",
                        brokerAccount.getId())
                        .with(csrf())
                        .with(user("analytics-user"))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrades").value(5))
                .andExpect(jsonPath("$.winningTrades").value(3))
                .andExpect(jsonPath("$.losingTrades").value(2))
                .andExpect(jsonPath("$.winRate").value(60.00))
                .andExpect(jsonPath("$.totalProfitLoss").value(1200))
                .andExpect(jsonPath("$.averageProfit").value(600.00))
                .andExpect(jsonPath("$.averageLoss").value(300.00))
                .andExpect(jsonPath("$.profitFactor").value(3.00));
    }

    private Trade createTrade(BrokerAccount brokerAccount,
                              String profitLoss) {

        Trade trade = new Trade();
        trade.setBrokerAccount(brokerAccount);
        trade.setProfitLoss(new BigDecimal(profitLoss));

        return trade;
    }

}
