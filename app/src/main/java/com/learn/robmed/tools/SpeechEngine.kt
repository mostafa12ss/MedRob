package com.learn.robmed.tools


interface SpeechEngine {
    fun speak(text: String)
    fun stop()
    fun release()
}