package com.example.tradelens.repository;

import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExecutionRepository extends JpaRepository<Execution, Long>{

    List<Execution> findByBrokerAccount(BrokerAccount brokerAccount);

    Optional<Execution> findByOrderId(String orderId);

}
