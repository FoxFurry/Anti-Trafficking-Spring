package com.example.analyzetrafficking

import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.protobuf.ByteString
import java.io.ByteArrayInputStream
import java.util.ArrayList

class ProcessImage constructor(bytes: ByteArray) {
    private val file = bytes

    private var erotic: Int = 0
    private var adult: Int = 0
    private var violence: Int = 0
    private var spoof: Int = 0

    private val spoofFactor: Double = 0.25
    private val adultFactor: Double = 0.5

    init{
        if(file.isNotEmpty())
            process()
        else
            println("Bad file")
    }

    private fun process(){
        val requests: MutableList<AnnotateImageRequest> = ArrayList()
        val imgBytes = ByteString.readFrom(ByteArrayInputStream(file))
        val img = Image.newBuilder().setContent(imgBytes).build()
        val feat = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build()
        val request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
        requests.add(request)
        ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateImages(requests)
            val responses =
                response.responsesList
            for (res in responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.error.message)
                    return
                }
                val annotation = res.safeSearchAnnotation

                erotic = annotation.racyValue
                spoof = annotation.spoofValue
                adult = annotation.adultValue
                violence = annotation.violenceValue
            }
        }
    }

    fun getResult(): Double{
        return (erotic + spoof * spoofFactor + adult * adultFactor)/8.25
    }

    fun getErotic(): Int{
        return erotic
    }

    fun getSpoof(): Int{
        return spoof
    }

    fun getAdult(): Int{
        return adult
    }

    fun getViolence(): Int{
        return violence
    }
}