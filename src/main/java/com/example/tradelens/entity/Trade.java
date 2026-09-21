package com.example.tradelens.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Instrument instrument;

    @ManyToOne
    private BrokerAccount brokerAccount;

    private BigDecimal quantity;

    private BigDecimal entryPrice;

    private BigDecimal exitPrice;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private BigDecimal profitLoss;

    @OneToOne
    private Decision decision;

}
