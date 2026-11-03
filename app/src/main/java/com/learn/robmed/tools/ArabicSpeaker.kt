package com.learn.robmed.tools


import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class ArabicSpeaker(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->

            if (status == TextToSpeech.SUCCESS) {

                val egyptian = Locale("ar", "EG")

                val result = tts?.setLanguage(egyptian)

                ready = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

                tts?.setSpeechRate(0.88f)
                tts?.setPitch(1.4f)
            }
        }
    }

    fun speak(text: String) {
        if (ready) {
            tts?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "robmed_${System.currentTimeMillis()}"
            )
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}