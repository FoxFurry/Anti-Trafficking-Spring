package com.example.analyzetrafficking

import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.client.RestTemplate
import kotlin.math.max


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

@RestController
class RestController {
    @PostMapping("/posts")
    fun post(@RequestBody postdt: PostData): ResponseEntity<ExtResponse> {
        val result = processData(postdt)
        val dt = ExtResponse((max(result.first, result.second) > 0.55))

        return ResponseEntity.ok(dt)
    }

    @PostMapping("/messages")
    fun message(@RequestBody postdt: PostData): ResponseEntity<ExtResponse> {
        val result = processData(postdt)
        val dt = ExtResponse((max(result.first, result.second) > 0.55))

        return ResponseEntity.ok(dt)
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

        return Pair(textValue, imageValue)
    }
}