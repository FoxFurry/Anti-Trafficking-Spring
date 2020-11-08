package com.example.analyzetrafficking

import javax.persistence.*


@Entity
data class Channel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column
    var name: String,

    @Column
    var type: String,

    @Column
    var value: String
){
    constructor() : this(null, "","","")
}