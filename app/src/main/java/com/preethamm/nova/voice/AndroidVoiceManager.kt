package com.preethamm.nova.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class AndroidVoiceManager(
    context: Context
) : VoiceManager, RecognitionListener, TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private var listener: VoiceListener? = null
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private var ttsReady = false

    override fun setListener(listener: VoiceListener?) {
        this.listener = listener
    }

    override fun startListening() {
        interruptSpeech()
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            listener?.onSpeechError("Speech recognition is not available on this device.")
            return
        }
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                setRecognitionListener(this@AndroidVoiceManager)
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Nova")
        }
        recognizer?.startListening(intent)
    }

    override fun stopListening() {
        recognizer?.stopListening()
    }

    override fun speak(text: String) {
        if (!ttsReady) {
            listener?.onTtsError("Text-to-speech is still starting. Response is shown on screen.")
            return
        }
        listener?.onTtsStarted()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    override fun interruptSpeech() {
        tts?.stop()
    }

    override fun shutdown() {
        recognizer?.destroy()
        recognizer = null
        tts?.shutdown()
        tts = null
        listener = null
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            tts?.language = Locale.getDefault()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = listener?.onTtsStarted() ?: Unit
                override fun onDone(utteranceId: String?) = listener?.onTtsFinished() ?: Unit
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) = listener?.onTtsError("Nova could not speak the response.") ?: Unit
                override fun onError(utteranceId: String?, errorCode: Int) =
                    listener?.onTtsError("Nova could not speak the response. Error $errorCode.") ?: Unit
            })
        } else {
            listener?.onTtsError("Text-to-speech failed to start.")
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        listener?.onSpeechReady()
    }

    override fun onBeginningOfSpeech() {
        listener?.onSpeechStarted()
    }

    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        listener?.onSpeechEnded()
    }

    override fun onError(error: Int) {
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Microphone audio error."
            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
            SpeechRecognizer.ERROR_NETWORK -> "Network error while listening."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition timed out."
            SpeechRecognizer.ERROR_NO_MATCH -> "I did not catch that. Try again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Try again in a moment."
            SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I did not hear anything."
            else -> "Speech recognition error $error."
        }
        listener?.onSpeechError(message)
    }

    override fun onResults(results: Bundle?) {
        val text = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            .orEmpty()
        if (text.isBlank()) {
            listener?.onSpeechError("I did not catch that. Try again.")
        } else {
            listener?.onSpeechResult(text)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private companion object {
        const val UTTERANCE_ID = "nova-response"
    }
}
