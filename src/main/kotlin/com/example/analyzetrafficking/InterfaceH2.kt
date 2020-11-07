package com.example.analyzetrafficking

import org.h2.engine.User
import org.springframework.data.jpa.repository.JpaRepository


interface HistoryRepo : JpaRepository<History?, Long?>{

}