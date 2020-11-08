package com.example.analyzetrafficking

import com.google.cloud.language.v1.ClassifyTextRequest
import com.google.cloud.language.v1.Document
import com.google.cloud.language.v1.LanguageServiceClient
import com.google.cloud.translate.TranslateOptions
import kotlin.math.max

class ProcessText constructor(inputString: String) {
    private var text = inputString

    private var adult: Float = 0.0F
    private var massage: Float = 0.0F
    private var dating: Float = 0.0F
    private var key_words_count: Int = 0
    private var n_points: Int = 0

    private val key_words = arrayOf("fuck","cum","massage","sex","sexual","sexy","sexuality","fucking","cumming","dick","cock","work","money")

    init{
        if(text.isNotEmpty())
            process()
        else
            println("Bad string")

    }

    private fun process(){
        translateText()
        val textSlices = text.split(' ').toMutableList()

        textSlices.forEach {
            if(it.toLowerCase() in key_words)key_words_count++
        }

        if(text.length < 100){
            if(textSlices.size < 20){
                val tempSlices = textSlices.toList()

                while(textSlices.size < 20){
                    textSlices += tempSlices
                }
                text = textSlices.joinToString(" ")
            }
        }

        LanguageServiceClient.create().use { language ->
            val doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build()
            val request = ClassifyTextRequest.newBuilder().setDocument(doc).build()
            // detect categories in the given text
            val response = language.classifyText(request)
            for (category in response.categoriesList) {
                when (category.name) {
                    "/Adult" -> adult = category.confidence
                    "/Beauty & Fitness/Spas & Beauty Services/Massage Therapy" -> massage = category.confidence
                    "/Online Communities/Dating & Personals" -> dating = category.confidence
                }
                n_points++
            }

        }
    }

    private fun translateText(){
        val translate = TranslateOptions.getDefaultInstance().service
        text = translate.translate(text).translatedText
    }

    fun getResults(): Double{
        return max((adult + massage + dating).toDouble() / n_points + key_words_count.toDouble()/10.0, 1.0)
    }

}