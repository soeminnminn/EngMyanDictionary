package com.s16.engmyan.utils

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.lang.Exception
import java.util.*

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {

    interface OnTextToSpeechListener {
        fun onTextToSpeechInit(enabled: Boolean)
    }

    private val textToSpeech = TextToSpeech(context, this)

    var text: String? = null

    var isEnabled: Boolean = false
        private set

    var onTextToSpeechListener : OnTextToSpeechListener? = null

    private val utteranceId: String
        get() = "${hashCode()}"

    override fun onInit(status: Int) {
        if (Build.MANUFACTURER.toLowerCase(Locale.ENGLISH) != "huawei") {
            isEnabled = status == TextToSpeech.SUCCESS
            if (isEnabled) {
                if (textToSpeech.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE) {
                    textToSpeech.language = Locale.ENGLISH
                } else {
                    isEnabled = false
                }
            }
            onTextToSpeechListener?.onTextToSpeechInit(isEnabled)
        }
    }

    @Suppress("DEPRECATION")
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

//    companion object {
//        @Volatile private var instance: TextToSpeechHelper? = null
//        private val LOCK = Any()
//
//        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
//            instance ?: TextToSpeechHelper(context).also { instance = it}
//        }
//    }
}