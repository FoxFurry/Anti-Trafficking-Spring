package com.example.analyzetrafficking

import javax.persistence.*

@Entity
data class Aggregate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column
    var susDays: Int,

    @Column
    var susWeeks: Int,

    @Column
    var dangDays: Int,

    @Column
    var dangWeeks: Int
){
    constructor() : this(null, 0,0,0,0)
}