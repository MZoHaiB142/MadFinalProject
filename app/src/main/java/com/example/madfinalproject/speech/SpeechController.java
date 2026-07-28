package com.example.madfinalproject.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import com.example.madfinalproject.utils.LogUtils;
import java.util.ArrayList;
import java.util.Locale;

public final class SpeechController implements RecognitionListener {
    private static final String TAG = "SpeechController";
    private final SpeechListener listener;
    private Intent recognizerIntent;
    private SpeechRecognizer recognizer;
    private boolean listening;

    public SpeechController(Context context, SpeechListener listener) {
        this(context, listener, Locale.getDefault());
    }

    public SpeechController(Context context, SpeechListener listener, Locale language) {
        this.listener = listener;
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError("Speech recognition is not available on this device.");
            return;
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(context.getApplicationContext());
        recognizer.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.toLanguageTag())
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                .putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                .putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L);
    }

    public void startListening() {
        if (recognizer == null) { listener.onError("Speech recognition is not available."); return; }
        if (listening) stopListening();
        try {
            listening = true;
            recognizer.startListening(recognizerIntent);
            LogUtils.d(TAG, "Listening started");
            listener.onListeningStarted();
        } catch (RuntimeException error) {
            listening = false;
            LogUtils.e(TAG, "Unable to start listening", error);
            listener.onError("Microphone could not be started. Please try again.");
        }
    }

    public void stopListening() {
        if (recognizer != null && listening) {
            recognizer.stopListening();
            listening = false;
            LogUtils.d(TAG, "Listening stopped");
            listener.onListeningStopped();
        }
    }

    public void cancelListening() {
        if (recognizer != null) recognizer.cancel();
        if (listening) listener.onListeningStopped();
        listening = false;
        LogUtils.d(TAG, "Listening cancelled");
    }

    public boolean isListening() { return listening; }

    public void destroy() {
        if (recognizer != null) { recognizer.cancel(); recognizer.destroy(); recognizer = null; }
        listening = false;
    }

    @Override public void onReadyForSpeech(Bundle params) { }
    @Override public void onBeginningOfSpeech() { }
    @Override public void onRmsChanged(float rmsdB) { }
    @Override public void onBufferReceived(byte[] buffer) { }
    @Override public void onEndOfSpeech() { listening=false; listener.onListeningStopped(); LogUtils.d(TAG,"Speech ended"); }

    @Override public void onError(int error) {
        listening=false;
        listener.onListeningStopped();
        String message=messageFor(error);
        LogUtils.e(TAG,"Recognition error " + error + ": " + message);
        listener.onError(message);
    }

    @Override public void onResults(Bundle results) {
        listening=false;
        String transcript=first(results);
        if(transcript.isEmpty()){listener.onError("No speech was detected. Please try again.");return;}
        LogUtils.d(TAG,"Transcript received: " + transcript);
        listener.onResult(transcript);
    }

    @Override public void onPartialResults(Bundle partialResults) {
        String transcript=first(partialResults);
        if(!transcript.isEmpty())listener.onPartialResult(transcript);
    }
    @Override public void onEvent(int eventType, Bundle params) { }

    private String first(Bundle bundle){if(bundle==null)return "";ArrayList<String> values=bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);return values==null||values.isEmpty()?"":values.get(0).trim();}
    private String messageFor(int error){switch(error){case SpeechRecognizer.ERROR_AUDIO:return "Microphone audio could not be captured.";case SpeechRecognizer.ERROR_CLIENT:return "Listening was cancelled.";case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:return "Microphone permission is required.";case SpeechRecognizer.ERROR_NETWORK:case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:return "Speech service needs an internet connection.";case SpeechRecognizer.ERROR_NO_MATCH:return "No clear speech was detected. Please speak again.";case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:return "Speech recognizer is busy. Please wait and retry.";case SpeechRecognizer.ERROR_SERVER:return "Speech recognition service is temporarily unavailable.";case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:return "No speech was heard. Tap the microphone and try again.";default:return "Speech could not be recognized. Please try again.";}}
}
