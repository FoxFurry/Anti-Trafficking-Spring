package com.example.analyzetrafficking

import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class MonthData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column
    var date: LocalDateTime,

    @Column
    var valueMessages: Int,

    @Column
    var valuePosts: Int
) {
    constructor() : this(null, LocalDateTime.now(), 0, 0)
}

