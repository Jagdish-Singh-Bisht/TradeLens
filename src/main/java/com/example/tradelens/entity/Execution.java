package com.example.tradelens.entity;


import com.example.tradelens.entity.enums.ExecutionSide;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;





@Entity
@Table(
        name = "executions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_execution_account_order",
                        columnNames = { "broker_account_id", "order_id" }
                )
        }
)
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

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    private ExecutionSide side;

    private BigDecimal quantity;

    private BigDecimal price;

    private LocalDateTime executedAt;

}
