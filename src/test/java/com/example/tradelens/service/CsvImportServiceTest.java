package com.example.tradelens.service;


import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.User;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.repository.UserRepository;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.TradeRepository;
import com.example.tradelens.repository.ExecutionRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
//import org.springframework.transaction.annotation.Transactional;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.argThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
//import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
//@Transactional
public class CsvImportServiceTest {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @MockitoSpyBean
    private TradeReconstructionService tradeReconstructionService;

    @Test
    void shouldSkipDuplicateExecutionsWhenSameCsvIsImportedTwice() {

        String username = "csv-import-test-" + UUID.randomUUID();

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("test123");

        user = userRepository.save(user);

        user = userRepository.save(user);

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setBrokerName("Test Broker");
        brokerAccount.setAccountName("CSV Test Account");
        brokerAccount.setUser(user);

        brokerAccount = brokerAccountRepository.save(brokerAccount);

        Instrument instrument = instrumentRepository
                .findBySymbolAndExchange("RELIANCE", "NSE")
                .orElseGet(() -> {
                    Instrument newInstrument = new Instrument();
                    newInstrument.setSymbol("RELIANCE");
                    newInstrument.setExchange("NSE");
                    newInstrument.setType(InstrumentType.EQUITY);
                    return instrumentRepository.save(newInstrument);
                });

        String csv = """
                order_id,symbol,exchange,side,quantity,price,executed_at
                TEST-BUY-INVALID-001,RELIANCE,NSE,BUY,10,1400.00,2026-09-23T09:30:00
                TEST-BAD-INVALID-001,RELIANCE,NSE,BUY,abc,1400.00,2026-09-23T09:45:00
                TEST-SELL-INVALID-001,RELIANCE,NSE,SELL,10,1450.00,2026-09-23T10:00:00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        csvImportService.importExecutions(
                brokerAccount.getId(),
                file,
                username
        );

        List<Trade> tradesAfterFirstImport =
                tradeRepository.findByBrokerAccount(brokerAccount);

        assertEquals(1, tradesAfterFirstImport.size());

        assertEquals(
                2,
                executionRepository.findByBrokerAccount(brokerAccount).size()
        );

        csvImportService.importExecutions(
                brokerAccount.getId(),
                file,
                username
        );

        List<Trade> tradesAfterSecondImport =
                tradeRepository.findByBrokerAccount(brokerAccount);

        assertEquals(1, tradesAfterSecondImport.size());

        assertEquals(
                2,
                executionRepository.findByBrokerAccount(brokerAccount).size()
        );

    }

    @Test
    void shouldRollbackAllDatabaseChangesWhenUnexpectedFailureOccursMidImport() {

        String username = "rollback-test-" + UUID.randomUUID();

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("test123");
        user = userRepository.save(user);

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setBrokerName("Test Broker");
        brokerAccount.setAccountName("Rollback Test Account");
        brokerAccount.setUser(user);
        brokerAccount = brokerAccountRepository.save(brokerAccount);

        final BrokerAccount savedBrokerAccount = brokerAccount;

        instrumentRepository.findBySymbolAndExchange("RELIANCE", "NSE")
                .orElseGet(() -> {
                    Instrument instrument = new Instrument();
                    instrument.setSymbol("RELIANCE");
                    instrument.setExchange("NSE");
                    instrument.setType(InstrumentType.EQUITY);
                    return instrumentRepository.save(instrument);
                });

        String csv = """
                order_id,symbol,exchange,side,quantity,price,executed_at
                ROLLBACK-BUY-001,RELIANCE,NSE,BUY,10,1400.00,2026-09-23T09:30:00
                ROLLBACK-SELL-001,RELIANCE,NSE,SELL,10,1450.00,2026-09-23T10:00:00
                ROLLBACK-BUY-002,RELIANCE,NSE,BUY,5,1420.00,2026-09-23T10:15:00
                ROLLBACK-FAIL-001,RELIANCE,NSE,SELL,5,1430.00,2026-09-23T10:30:00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rollback-test.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        doThrow(new IllegalArgumentException("Simulated reconstruction failed"))
                .when(tradeReconstructionService)
                .processExecution(argThat(execution ->
                        "ROLLBACK-FAIL-001".equals(execution.getOrderId())
                ));

//        assertEquals(
//                false,
//                TransactionSynchronizationManager.isActualTransactionActive(),
//                "The test itself should not already be inside a transaction"
//        );

        assertThrows(IllegalArgumentException.class,
                () -> csvImportService.importExecutions(
                        savedBrokerAccount.getId(), file, username
                )
        );

        assertEquals(
                0,
                executionRepository.findByBrokerAccount(brokerAccount).size(),
                "All executions from the failed import should be rolled back"
        );

        assertEquals(
                0,
                tradeRepository.findByBrokerAccount(brokerAccount).size(),
                "Trades created earlier in the failed import should also be rolled back"
        );

        String recoveryBuyCsv = """
                order_id,symbol,exchange,side,quantity,price,executed_at
                RECOVERY-BUY-001,RELIANCE,NSE,BUY,7,1400.00,2026-09-24T09:30:00
                """;

        MockMultipartFile recoveryBuyFile = new MockMultipartFile(
                "file",
                "recovery-buy.csv",
                "text/csv",
                recoveryBuyCsv.getBytes(StandardCharsets.UTF_8)
        );

        csvImportService.importExecutions(
                savedBrokerAccount.getId(),
                recoveryBuyFile,
                username
        );

        assertEquals(
                1,
                executionRepository.findByBrokerAccount(savedBrokerAccount).size(),
                "Only the recovery BUY should remain after the failed import"
        );

        assertEquals(
                0,
                tradeRepository.findByBrokerAccount(savedBrokerAccount).size()
        );

        String recoverySellCsv = """
    order_id,symbol,exchange,side,quantity,price,executed_at
    RECOVERY-SELL-001,RELIANCE,NSE,SELL,7,1450.00,2026-09-24T10:00:00
    """;

        MockMultipartFile recoverySellFile = new MockMultipartFile(
                "file",
                "recovery-sell.csv",
                "text/csv",
                recoverySellCsv.getBytes(StandardCharsets.UTF_8)
        );

        csvImportService.importExecutions(
                savedBrokerAccount.getId(),
                recoverySellFile,
                username
        );

        assertEquals(
                2,
                executionRepository.findByBrokerAccount(savedBrokerAccount).size(),
                "The recovery BUY and SELL should both be persisted"
        );

        assertEquals(
                1,
                tradeRepository.findByBrokerAccount(savedBrokerAccount).size(),
                "The recovery SELL should create exactly one trade"
        );

    }

}
