# Task: Add Logging & Silence Client Error
- [x] Added `Log.d` and `Log.e` to `VoiceInputViewModel.kt` for debugging.
- [x] Modified `onError` to handle `SpeechRecognizer.ERROR_CLIENT` (5) silently since it's being auto-recovered.
- [x] Added logging to `startListening` and `onResults` to trace flow.
