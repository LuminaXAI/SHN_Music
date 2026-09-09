# SHN Music 🎵

A modern, premium Android music player built with Kotlin and Jetpack Compose.

SHN Music is designed as a fast, elegant and privacy-friendly local music player with a futuristic user experience, powerful library management and smooth media playback.

## ✨ Features

- 🎵 Local music library scanning
- 🎧 High-quality audio playback
- 📚 Songs, Albums, Artists and Folders
- 🔎 Fast music search
- ▶️ Queue management
- ❤️ Playlists and library organization
- 🎚️ Built-in equalizer
- ⏯️ Modern mini player and full player
- 🔔 Media playback service
- 🌙 Premium dark UI
- 🌍 Arabic RTL support
- 📱 Responsive Jetpack Compose interface

## 🛠️ Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android Media3 / ExoPlayer
- Room Database
- DataStore
- Kotlin Coroutines
- Dependency Injection architecture
- Android SDK 37

## 🏗️ Architecture

SHN Music follows a modular architecture with clear separation between:

- UI
- Features
- Domain
- Data
- Database
- Media Playback
- Settings
- Audio / Equalizer

## 📂 Project Structure

```text
app/
├── src/main/java/com/shn/music/
│   ├── core/
│   │   ├── audio/
│   │   ├── database/
│   │   ├── media/
│   │   └── settings/
│   ├── data/
│   ├── domain/
│   ├── feature/
│   └── ui/
│       ├── components/
│       ├── screens/
│       ├── strings/
│       └── theme/
└── src/main/res/
    ├── mipmap-mdpi/
    ├── mipmap-hdpi/
    ├── mipmap-xhdpi/
    ├── mipmap-xxhdpi/
    ├── mipmap-xxxhdpi/
    ├── values/
    └── values-ar/
```

## 🚀 Build

Clone the repository:

```bash
git clone https://github.com/LuminaXAI/SHN_Music.git
cd SHN_Music
```

Build the debug APK:

```powershell
.\gradlew assembleDebug
```

The generated APK will be located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 📱 Requirements

- Android Studio
- JDK 17+
- Android SDK 37
- Android API 24+

## 🌐 Localization

SHN Music includes Arabic localization and RTL-aware interface support.

## 🔐 Privacy

SHN Music is designed around local music playback and library management. The core playback experience does not require uploading the user music to a cloud service.

## 📌 Project Status

🚧 Active development

The project is continuously evolving toward a polished, production-ready Android music player.

## 👨‍💻 Author

**LuminaXAI**

GitHub: https://github.com/LuminaXAI

---

⭐ Star the repository if you like the project.
