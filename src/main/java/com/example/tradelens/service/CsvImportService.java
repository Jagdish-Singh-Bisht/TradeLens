package com.example.tradelens.service;



import com.example.tradelens.exception.InvalidCsvException;
import com.example.tradelens.dto.ExecutionCsvRow;
import com.example.tradelens.entity.enums.ExecutionSide;
import com.example.tradelens.entity.Execution;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.ExecutionRepository;
import com.example.tradelens.repository.InstrumentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.math.BigDecimal;
import java.time.LocalDateTime;





@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final BrokerAccountRepository brokerAccountRepository;

    private final InstrumentRepository instrumentRepository;

    private final ExecutionRepository executionRepository;

    @Transactional
    public void importExecutions(Long brokerAccountId, MultipartFile file) {

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                file.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                CSVParser parser = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
                        .parse(reader);
        ) {

            BrokerAccount brokerAccount = brokerAccountRepository
                    .findById(brokerAccountId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Broker account not found"
                    ));

            for (CSVRecord record : parser) {

                ExecutionCsvRow row = new ExecutionCsvRow(
                        record.get("order_id"),
                        record.get("symbol"),
                        record.get("exchange"),
                        record.get("side"),
                        new BigDecimal(record.get("quantity")),
                        new BigDecimal(record.get("price")),
                        LocalDateTime.parse(record.get("executed_at"))
                );

                Instrument instrument = instrumentRepository
                        .findBySymbolAndExchange(
                                row.symbol(),
                                row.exchange()
                        )
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Instrument not found: "
                                        + row.symbol()
                                        + " on "
                                        + row.exchange()
                                ));

                Execution execution = new Execution();

                execution.setBrokerAccount(brokerAccount);
                execution.setInstrument(instrument);
                execution.setOrderId(row.orderId());
                execution.setSide(
                        ExecutionSide.valueOf(row.side())
                );
                execution.setQuantity(row.quantity());
                execution.setPrice(row.price());
                execution.setExecutedAt(row.executedAt());

                executionRepository.save(execution);

                System.out.println(row);
            }

        } catch(Exception e) {
            throw new InvalidCsvException(
                    "Failed to read CSV file",
                    e
            );
        }


    }
}
