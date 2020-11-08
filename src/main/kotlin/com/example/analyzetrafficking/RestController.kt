package com.example.analyzetrafficking

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import kotlin.math.max
import org.springframework.web.bind.annotation.PathVariable

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


@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
@RestController
class RestController constructor(history: HistoryRepo, channels: ChannelsRepo) {
    private val historyRepo = history
    private val channelsRepo = channels

    // POSTS

    @PostMapping("/posts")
    fun posts(@RequestBody postdt: PostData): ResponseEntity<ExtResponse> {
        val result = processData(postdt)
        val dt = ExtResponse(max(result.first, result.second) > 0.55)

        return ResponseEntity.ok(dt)
    }

    // MESSAGES

    @PostMapping("/messages")
    fun messages(@RequestBody postdt: PostData): ResponseEntity<ExtResponse> {
        val result = processData(postdt)
        val dt = ExtResponse(max(result.first, result.second) > 0.55)

        return ResponseEntity.ok(dt)
    }

    // CHANNELS

    @PostMapping("/channels")
    fun channels_post(@RequestBody channelDt: Channel): ResponseEntity<Void>{
        channelsRepo.save(channelDt)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/channels")
    fun channels_read(): ResponseEntity<MutableList<Channel>> {
        val channel_buffer = channelsRepo.findAll()
        return ResponseEntity.ok(channel_buffer)
    }

    // HISTORY

    @GetMapping("/history")
    fun history_get(): ResponseEntity<MutableList<History>>{
        val history_buffer = historyRepo.findAll()
        return ResponseEntity.ok(history_buffer)
    }


    private fun processData(postdt: PostData): Pair<Double, Double>{
        var textValue: Double = 0.0
        var imageValue: Double = 0.0

        // Text processing
        if(postdt.text != "null"){
            val textProcess = ProcessText(postdt.text)
            textValue = textProcess.getResults()

            //println(textValue)
        }
        // Image processing
        val restTemplate = RestTemplate()
        var maxImageResult: Double = 0.0
        postdt.media.forEach{
            if(it.type=="photo") {
                val imageBytes = restTemplate.getForObject(it.url, ByteArray::class.java)!!
                val imageProcess = ProcessImage(imageBytes)
                maxImageResult = max(maxImageResult, imageProcess.getResult())
                //println(imageProcess.getResult())
            }
        }
        imageValue = maxImageResult

        val historyVal = History(null, LocalDateTime.now(), max(imageValue, textValue))
        historyRepo.save(historyVal)
        return Pair(textValue, imageValue)
    }
}