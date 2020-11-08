package com.example.analyzetrafficking

import org.springframework.data.jpa.repository.JpaRepository

interface HistoryRepo : JpaRepository<History, Long>