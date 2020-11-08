package com.example.analyzetrafficking

import org.springframework.data.jpa.repository.JpaRepository

interface ChannelsRepo : JpaRepository<Channel, Long>