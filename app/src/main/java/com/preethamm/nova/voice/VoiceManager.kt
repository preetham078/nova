package com.preethamm.nova.voice

interface VoiceManager {
    fun setListener(listener: VoiceListener?)
    fun startListening()
    fun stopListening()
    fun speak(text: String)
    fun interruptSpeech()
    fun shutdown()
}
