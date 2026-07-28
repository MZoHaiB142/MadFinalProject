package com.example.madfinalproject.speech;

public interface SpeechListener {
    void onListeningStarted();
    void onListeningStopped();
    void onResult(String transcript);
    void onPartialResult(String transcript);
    void onError(String message);
}
