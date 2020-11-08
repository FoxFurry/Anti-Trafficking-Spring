package com.example.analyzetrafficking

import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class History(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column
    var date: LocalDateTime,

    @Column
    var value: Double
) {
    constructor() : this(null, LocalDateTime.now(), 0.0)
}

