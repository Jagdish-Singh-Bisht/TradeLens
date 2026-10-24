package com.example.tradelens.service;



import com.example.tradelens.dto.CsvImportReport;
import com.example.tradelens.exception.InvalidCsvException;
import com.example.tradelens.dto.ExecutionCsvRow;
import com.example.tradelens.entity.enums.ExecutionSide;
import com.example.tradelens.entity.Execution;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.ExecutionRepository;
import com.example.tradelens.repository.InstrumentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final BrokerAccountRepository brokerAccountRepository;

    private final InstrumentRepository instrumentRepository;

    private final ExecutionRepository executionRepository;

    private final TradeReconstructionService tradeReconstructionService;

    @Transactional
    public CsvImportReport importExecutions(Long brokerAccountId,
                                 MultipartFile file,
                                 String username) {

        BrokerAccount brokerAccount = brokerAccountRepository
                .findByIdAndUser_Username(brokerAccountId, username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Broker account not found"
                ));

        List<CsvImportReport.RowError> errors = new ArrayList<>();
        List<ValidatedRow> validRows = new ArrayList<>();
        Set<String> seenOrderIds = new HashSet<>();

        int totalRows = 0;
        int skippedRows = 0;

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
            //            List<ExecutionCsvRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {

                totalRows++;

                long rowNumber = record.getRecordNumber() + 1;

                try {

                    ExecutionCsvRow row = new ExecutionCsvRow(
                            record.get("order_id").trim(),
                            record.get("symbol").trim(),
                            record.get("exchange").trim(),
                            record.get("side").trim(),
                            new BigDecimal(record.get("quantity").trim()),
                            new BigDecimal(record.get("price").trim()),
                            LocalDateTime.parse(record.get("executed_at").trim())
                    );

                    if (row.orderId().isBlank()
                            || row.symbol().isBlank()
                            || row.exchange().isBlank()) {

                        throw new IllegalArgumentException(
                                "Order ID, symbol and exchange are required"
                        );

                    }

                    if (row.quantity().signum() <= 0
                            || row.price().signum() <= 0) {

                        throw new IllegalArgumentException(
                                "Quantity and price must be greater than zero"
                        );
                    }

                    ExecutionSide side = ExecutionSide.valueOf(row.side());

                    Instrument instrument = instrumentRepository.findBySymbolAndExchange(
                                    row.symbol(),
                                    row.exchange()
                            )
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Instrument not found: "
                                            + row.symbol()
                                            + " on "
                                            + row.exchange()
                            ));

                    if (executionRepository
                            .findByBrokerAccountAndOrderId(
                                    brokerAccount,
                                    row.orderId()
                            )
                            .isPresent()) {

                        skippedRows++;
                        continue;
                    }

                    if (!seenOrderIds.add(row.orderId())) {
                        skippedRows++;
                        continue;
                    }

                    validRows.add(new ValidatedRow(
                            rowNumber, row, instrument, side
                    ));

                } catch (Exception e) {
                    errors.add(new CsvImportReport.RowError(
                            rowNumber,
                            e.getMessage() == null
                                    ? "Invalid CSV row"
                                    : e.getMessage()
                    ));
                }
            }

        } catch (Exception e) {
            throw new InvalidCsvException(
                    "Failed to read CSV file: " + e.getMessage(), e
            );
        }

        int importedRows = 0;

        for (ValidatedRow validated : validRows) {

            ExecutionCsvRow row = validated.row();

            Execution execution = new Execution();
            execution.setBrokerAccount(brokerAccount);
            execution.setInstrument(validated.instrument());
            execution.setOrderId(row.orderId());
            execution.setSide(validated.side());
            execution.setQuantity(row.quantity());
            execution.setPrice(row.price());
            execution.setExecutedAt(row.executedAt());

            Execution savedExecution = executionRepository.save(execution);

            tradeReconstructionService.processExecution(savedExecution);

            importedRows++;

        }

        return new CsvImportReport(
                totalRows,
                importedRows,
                skippedRows,
                errors.size(),
                errors
        );

    }

    private record ValidatedRow(
            long rowNumber,
            ExecutionCsvRow row,
            Instrument instrument,
            ExecutionSide side
    ) { }

}
