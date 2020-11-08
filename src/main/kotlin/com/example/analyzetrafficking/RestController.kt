package com.example.analyzetrafficking

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import kotlin.math.max

import org.springframework.web.bind.annotation.GetMapping
import com.twilio.Twilio
import com.twilio.type.PhoneNumber
import com.twilio.rest.api.v2010.account.Message;
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct
import kotlin.math.abs
import kotlin.random.Random


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

    @PostConstruct
    fun generate() {
        for(i in 31 downTo 0){
            val time = LocalDateTime.now().minusDays(i.toLong())
            val temp = History()
            temp.type = if(Random.nextInt(0,10) > 5) "message" else "post"
            temp.value = Random.nextDouble(0.0,1.0)
            temp.date = LocalDateTime.from(time)
            historyRepo.save(temp)
        }
    }


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

    @GetMapping("/history")
    fun get_histiry(): ResponseEntity<Array<History>>{
        val history_buffer = historyRepo.findAll()
        return ResponseEntity.ok(history_buffer.toTypedArray())
    }

    @GetMapping("/month")
    fun history_month(): ResponseEntity<Array<MonthData>> {
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

    @PostMapping("/message")
    fun message_num(@RequestParam(value = "number", defaultValue = "") number: String): ResponseEntity<Void> {
        if(number.isNotEmpty()){
            val ACCOUNT_SID = "ACfe446b551c8e937dabe377f40a0214ea"
            val AUTH_TOKEN = "b37b7646659f033ad3832a38b6777e55"

            Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
            val message: Message = Message.creator(
                PhoneNumber("whatsapp:+$number"),
                PhoneNumber("whatsapp:+14155238886"),
                "Hello! We found some strange activities on your child account! Please, check our app"
            )
                .create()
            //System.out.println(message.getSid())
            return ResponseEntity.ok().build()

        }
        return ResponseEntity.notFound().build()
    }



    private fun getMonth(history_buffer: MutableList<History>): Array<MonthData> {

        var dt = HashMap<LocalDate, Datum>()

        var reversed = history_buffer.reversed()

        var time = reversed[0].date.toLocalDate()

        reversed.forEach{
            when(it.type){
                "message"-> dt[it.date.toLocalDate()].message++
                "post"->dt[it.date.toLocalDate()].post++
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