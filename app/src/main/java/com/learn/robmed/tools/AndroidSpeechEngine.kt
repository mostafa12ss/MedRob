package com.learn.robmed.tools

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class AndroidSpeechEngine(
    context: Context,
    private val preferredVoiceName: String? = null
) : SpeechEngine {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var released = false

    private val pendingMessages = mutableListOf<String>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->

            if (released) return@TextToSpeech

            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech

                val languageResult =
                    engine.setLanguage(Locale("ar", "EG"))

                if (
                    languageResult != TextToSpeech.LANG_MISSING_DATA &&
                    languageResult != TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    selectArabicVoice(engine)

                    engine.setSpeechRate(1f)
                    engine.setPitch(1.4f)

                    ready = true

                    val messages = pendingMessages.toList()
                    pendingMessages.clear()

                    messages.forEach { message ->
                        speak(message)
                    }
                }
            }
        }
    }

    private fun selectArabicVoice(engine: TextToSpeech) {
        val availableVoices: Set<Voice> =
            engine.voices ?: emptySet()

        // لو حددنا اسم صوت يدويًا، نستخدمه إن كان متاحًا.
        val manuallySelectedVoice = preferredVoiceName?.let { name ->
            availableVoices.firstOrNull { it.name == name }
        }

        val egyptianVoice = availableVoices
            .filter {
                it.locale.language == "ar" &&
                        it.locale.country.equals("EG", ignoreCase = true)
            }
            .sortedByDescending { it.quality }
            .firstOrNull()

        val arabicVoice = availableVoices
            .filter { it.locale.language == "ar" }
            .sortedByDescending { it.quality }
            .firstOrNull()

        val selectedVoice =
            manuallySelectedVoice ?: egyptianVoice ?: arabicVoice

        selectedVoice?.let {
            engine.voice = it
        }
    }

    // استخدمها لمعرفة الأصوات التي يدعمها الجهاز.
    fun getAvailableArabicVoices(): List<String> {
        return tts?.voices
            ?.filter { it.locale.language == "ar" }
            ?.map {
                "${it.name} | ${it.locale} | quality=${it.quality}"
            }
            ?: emptyList()
    }

    override fun speak(text: String) {
        if (released || text.isBlank()) return

        if (!ready) {
            pendingMessages.add(text)
            return
        }

        tts?.speak(
            text,
            TextToSpeech.QUEUE_ADD,
            null,
            "robmed_${System.nanoTime()}"
        )
    }

    override fun stop() {
        pendingMessages.clear()
        tts?.stop()
    }

    override fun release() {
        if (released) return

        released = true
        ready = false
        pendingMessages.clear()

        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}