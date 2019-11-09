package com.s16.engmyan.utils

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.lang.Exception
import java.util.*

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {

    private val textToSpeech = TextToSpeech(context, this)

    var text: String? = null

    var isEnabled: Boolean = false
        private set

    private val utteranceId: String
        get() = "${hashCode()}"

    override fun onInit(status: Int) {
        isEnabled = status == TextToSpeech.SUCCESS
        if (isEnabled) {
            textToSpeech.language = Locale.US
        }
    }

    fun speak() {
        text?.let {
            if (isEnabled && it.isNotEmpty()) {
                val word = it.trim().replace("^[^a-zA-Z0-9]+(.*)".toRegex(), "$1")

                try {
                    if (Build.VERSION.SDK_INT >= 21) {
                        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, Bundle.EMPTY, utteranceId)
                    } else {
                        val key = TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
                        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, hashMapOf(key to utteranceId))
                    }

                } catch (e: Exception) {}
            }
        }
    }

    fun shutdown() {
        if (isEnabled) {
            textToSpeech.shutdown()
        }
    }
}