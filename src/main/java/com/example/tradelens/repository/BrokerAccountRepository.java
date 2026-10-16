package com.example.tradelens.repository;


import com.example.tradelens.entity.BrokerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BrokerAccountRepository extends JpaRepository<BrokerAccount, Long> {

    List<BrokerAccount> findByUser_Username(String username);

    Optional<BrokerAccount> findByIdAndUser_Username(Long id, String username);

}
