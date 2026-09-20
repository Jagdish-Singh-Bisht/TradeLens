package com.example.tradelens.repository;


import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByBrokerAccount(BrokerAccount brokerAccount);

}
