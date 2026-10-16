# Nova

Nova is a Phase 1 Android prototype for a privacy-respecting personal AI voice assistant.

## Phase 1 Features

- Jetpack Compose UI with Home, Connected Accounts, and Settings screens
- Speech-to-text with Android `SpeechRecognizer`
- AI service abstraction with OpenAI and demo implementations
- Text-to-speech responses with Android `TextToSpeech`
- In-memory conversation context
- Microphone permission handling and visible assistant states: `IDLE`, `LISTENING`, `THINKING`, `SPEAKING`, `ERROR`

## Configure OpenAI

Nova does not embed API keys into the APK. Open the app, go to Settings, paste your OpenAI API key, and save it. The key is encrypted locally with Android Keystore before being stored.

Without a saved key, Nova runs in demo mode and still exercises the UI, voice loop, and conversation flow.

## Run

Open the folder in Android Studio and run the `app` configuration, or use:

```sh
./gradlew assembleDebug
```

The shell is prepared for later phases: Room memory, Gmail OAuth, notification access, confirmation flows, and privacy controls.
# nova
