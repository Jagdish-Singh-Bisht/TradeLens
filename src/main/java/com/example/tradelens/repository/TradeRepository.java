package com.example.tradelens.repository;


import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByBrokerAccount(BrokerAccount brokerAccount);

    List<Trade> findByBrokerAccount_IdAndBrokerAccount_User_Username(
            Long brokerAccountId,
            String username
    );

    Optional<Trade> findByIdAndBrokerAccount_User_Username(
            Long tradeId,
            String username
    );

}
