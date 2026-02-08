package com.duetduetku.app.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duetduetku.app.util.GeminiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class VoiceInputState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    val analysisResult: GeminiHelper.ReceiptResult? = null
)

@HiltViewModel
class VoiceInputViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel(), RecognitionListener {

    private val _uiState = MutableStateFlow(VoiceInputState())
    val uiState: StateFlow<VoiceInputState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    init {
        // Initialize on main thread if needed, or lazily
    }

    // Track text from previous sessions
    private var accumulatedText = ""

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _uiState.value = _uiState.value.copy(error = "Speech recognition not available on this device")
            return
        }

        // Snapshot current text so we append to it
        accumulatedText = _uiState.value.spokenText

        if (speechRecognizer == null) {
            setupRecognizer()
        }
        
        _uiState.value = _uiState.value.copy(error = null, analysisResult = null)

        // Ensure we run on main thread
        android.os.Handler(android.os.Looper.getMainLooper()).post {
             try {
                 speechRecognizer?.startListening(speechIntent)
                 _uiState.value = _uiState.value.copy(isListening = true)
             } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(isListening = false, error = "Failed to start: ${e.message}")
             }
        }
    }



    fun stopListening() {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
             try {
                speechRecognizer?.stopListening()
             } catch (e: Exception) {
                // Ignore
             }
        }
        _uiState.value = _uiState.value.copy(isListening = false)
    }

    private fun setupRecognizer() {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(this)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Init failed: ${e.message}")
            }
        }
    }

    fun onManualTextChange(text: String) {
        _uiState.value = _uiState.value.copy(spokenText = text)
    }

    fun analyze() {
        val text = _uiState.value.spokenText
        if (text.isBlank()) return

        stopListening()
        _uiState.value = _uiState.value.copy(isAnalyzing = true, error = null)
        
        viewModelScope.launch {
            try {
                val result = GeminiHelper.analyzeText(text)
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = "Failed to analyze: ${e.message}"
                )
            }
        }
    }
    
    fun resetAnalysisState() {
        _uiState.value = _uiState.value.copy(analysisResult = null, error = null, isListening = false)
    }

    override fun onCleared() {
        super.onCleared()
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            speechRecognizer?.destroy()
        }
    }

    // RecognitionListener Implementation
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        _uiState.value = _uiState.value.copy(isListening = false)
    }
    
    override fun onError(error: Int) {
        val message = when(error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            SpeechRecognizer.ERROR_CLIENT -> "Client error (5)"
            else -> "Error $error"
        }
        
        if (error == SpeechRecognizer.ERROR_CLIENT) {
             // Re-create gracefully and silent the error for UI since we are recovering
             android.os.Handler(android.os.Looper.getMainLooper()).post {
                 speechRecognizer?.destroy()
                 speechRecognizer = null
             }
             // Don't show error to user, just stop listening state
             _uiState.value = _uiState.value.copy(isListening = false)
             return
        } else if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            // These are normal, just stop listening
            _uiState.value = _uiState.value.copy(isListening = false)
            return
        }
        
        _uiState.value = _uiState.value.copy(isListening = false, error = message)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val newText = matches[0]
            val fullText = if (accumulatedText.isBlank()) newText else "$accumulatedText $newText"
            _uiState.value = _uiState.value.copy(spokenText = fullText.trim())
        }
        _uiState.value = _uiState.value.copy(isListening = false)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val newText = matches[0]
            val fullText = if (accumulatedText.isBlank()) newText else "$accumulatedText $newText"
            _uiState.value = _uiState.value.copy(spokenText = fullText.trim())
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
