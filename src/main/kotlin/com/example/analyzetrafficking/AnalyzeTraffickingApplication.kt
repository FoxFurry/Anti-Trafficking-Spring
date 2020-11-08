package com.example.analyzetrafficking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync


@SpringBootApplication
class AnalyzeTraffickingApplication


fun main(args: Array<String>) {
	runApplication<AnalyzeTraffickingApplication>(*args)
}
