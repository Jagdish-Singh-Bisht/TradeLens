package com.example.tradelens.entity;


import com.example.tradelens.entity.enums.InstrumentType;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "instruments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_instrument_symbol_exchange",
                        columnNames = {"symbol", "exchange"}
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private String exchange;

    @Enumerated(EnumType.STRING)
    private InstrumentType type;

}
