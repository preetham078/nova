package com.preethamm.nova.voice

interface VoiceListener {
    fun onSpeechReady()
    fun onSpeechStarted()
    fun onSpeechResult(text: String)
    fun onSpeechError(message: String)
    fun onSpeechEnded()
    fun onTtsStarted()
    fun onTtsFinished()
    fun onTtsError(message: String)
}
