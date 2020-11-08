package com.example.analyzetrafficking

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import kotlin.math.max

import org.springframework.web.bind.annotation.GetMapping

data class Author(
    val name: String,
    val id: String
)

data class Media(
    val type: String,
    val url: String
)

data class PostData(
    val text: String,
    val media: List<Media>,
    val author: Author
)

data class ExtResponse(
    var suspicious: Boolean
)

@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
@RestController
class RestController constructor(history: HistoryRepo, channels: ChannelsRepo) {
    private val historyRepo = history
    private val channelsRepo = channels

    // POSTS

    @PostMapping("/posts")
    fun posts(@RequestBody postdt: PostData): ResponseEntity<ExtResponse> {
        val result = processData(postdt, "post")
        val dt = ExtResponse(max(result.first, result.second) > 0.55)

        return ResponseEntity.ok(dt)
    }

    // MESSAGES

    @PostMapping("/messages")
    fun messages(@RequestBody postdt: PostData): ResponseEntity<ExtResponse> {
        val result = processData(postdt, "message")
        val dt = ExtResponse(max(result.first, result.second) > 0.55)

        return ResponseEntity.ok(dt)
    }

    // CHANNELS

    @PostMapping("/channels")
    fun channels_post(@RequestBody channelDt: Channel): ResponseEntity<Void> {
        channelsRepo.save(channelDt)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/channels")
    fun channels_read(): ResponseEntity<MutableList<Channel>> {
        val channel_buffer = channelsRepo.findAll()
        return ResponseEntity.ok(channel_buffer)
    }

    // HISTORY

    @GetMapping("/day")
    fun history_day(): ResponseEntity<Array<History>> {
        val history_buffer = historyRepo.findAll()
        val dt = Array(24) { _ -> History() }

        history_buffer.forEach {
            if (it.date > LocalDateTime.now().minusHours(24)) {
                val index = 23 - (LocalDateTime.now().hour - it.date.hour).toInt()
                if (dt[index].value < it.value) dt[index] = it
            }
        }

        return ResponseEntity.ok(dt)
    }

    @GetMapping("/week")
    fun history_week(): ResponseEntity<Array<History>> {
        val history_buffer = historyRepo.findAll()
        val dt = getMonth(history_buffer).toList().takeLast(7).toTypedArray()
        return ResponseEntity.ok(dt)
    }

    @GetMapping("/month")
    fun history_month(): ResponseEntity<Array<History>> {
        val history_buffer = historyRepo.findAll()
        val dt = getMonth(history_buffer)
        return ResponseEntity.ok(dt)
    }

    @GetMapping("/aggr")
    fun aggregate(): ResponseEntity<Aggregate> {
        val history_buffer = historyRepo.findAll()
        val output = Aggregate()

        history_buffer.forEach {
            if (it.date > LocalDateTime.now().minusHours(24)) {
                if (it.value > 0.55) {

                    if (it.value > 0.75) output.dangDays++
                    else
                        output.susDays++
                }
            } else if (it.date > LocalDateTime.now().minusDays(7)) {
                if (it.value > 0.55) {

                    if (it.value > 0.75) output.dangWeeks++
                    else
                        output.susWeeks++
                }
            }
        }
        return ResponseEntity.ok(output)
    }

    private fun getMonth(history_buffer: MutableList<History>): Array<History> {
        val dt = Array(31) { _ -> History() }

        history_buffer.forEach {
            if (it.date > LocalDateTime.now().minusDays(31)) {
                val index = 30 - (LocalDateTime.now().dayOfMonth - it.date.dayOfMonth).toInt()
                if (dt[index].value < it.value) dt[index] = it
            }
        }
        return dt
    }

    private fun processData(postdt: PostData, type: String): Pair<Double, Double> {
        var textValue: Double = 0.0
        var imageValue: Double = 0.0

        // Text processing

        if (postdt.text != "null") {
            val textProcess = ProcessText(postdt.text)
            textValue = textProcess.getResults()
            //println(textValue)
        }

        // Image processing

        val restTemplate = RestTemplate()
        var maxImageResult: Double = 0.0
        postdt.media.forEach {
            if (it.type == "photo") {
                val imageBytes = restTemplate.getForObject(it.url, ByteArray::class.java)!!
                val imageProcess = ProcessImage(imageBytes)
                maxImageResult = max(maxImageResult, imageProcess.getResult())
                //println(imageProcess.getResult())
            }
        }
        imageValue = maxImageResult

        val historyVal = History(null, LocalDateTime.now(), max(imageValue, textValue), type)
        historyRepo.save(historyVal)
        return Pair(textValue, imageValue)
    }
}