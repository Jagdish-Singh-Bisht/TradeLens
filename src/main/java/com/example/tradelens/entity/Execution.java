package com.example.tradelens.entity;


import com.example.tradelens.entity.enums.ExecutionSide;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;





@Entity
@Table(name = "executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private BrokerAccount brokerAccount;

    @ManyToOne
    private Instrument instrument;

    private String orderId;

    @Enumerated(EnumType.STRING)
    private ExecutionSide side;

    private BigDecimal quantity;

    private BigDecimal price;

    private LocalDateTime executedAt;

}
