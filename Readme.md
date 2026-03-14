# 🤖 Android AI Examples

A collection of **10 production-ready AI feature implementations** for Android using **Jetpack Compose**, built step by step for developers who want to integrate AI into their Android apps.

> Built with ❤️ by [jinals-p](https://github.com/jinals-p)

---

## 📱 Screenshots

> Coming soon — screen recordings and screenshots will be added for each module.

---

## 🗂️ Modules

| # | Module | Tech | Description | Status |
|---|--------|------|-------------|--------|
| 01 | [QR / Barcode Scanner](#01-qr--barcode-scanner) | ML Kit | Scan any QR code or barcode in real time | ✅ Done |
| 02 | [Face Detection](#02-face-detection) | ML Kit | Detect faces, emotions, eye state and head direction | ✅ Done |
| 03 | [Text Recognition](#03-text-recognition) | ML Kit | Read and extract text from any real-world surface | ✅ Done |
| 04 | [Image Labeling](#04-image-labeling) | ML Kit | Identify and label objects in real time | ✅ Done |
| 05 | [Photo Explainer](#05-photo-explainer) | Gemini API | AI describes any image in natural language | ✅ Done |
| 06 | [AI Chat](#06-ai-chat) | Gemini API | Full chatbot with streaming responses and memory | ✅ Done |
| 07 | [Voice Assistant](#07-voice-assistant) | Gemini API | Talk to AI with your voice and hear it respond | ⏳ Coming Soon |
| 08 | [PDF Summarizer](#08-pdf-summarizer) | Gemini API | Summarize any document with AI | ⏳ Coming Soon |
| 09 | [Custom AI Model](#09-custom-ai-model) | TFLite | Run your own trained AI model completely offline | ⏳ Coming Soon |
| 10 | [Complete AI App](#10-complete-ai-app) | All | Full production AI app combining everything | ⏳ Coming Soon |

---

## ⚡ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (100% — no XML)
- **Architecture:** MVVM + StateFlow + ViewModel
- **AI/ML:** ML Kit · Gemini API · TensorFlow Lite
- **Camera:** CameraX
- **Navigation:** Jetpack Navigation Compose
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35
- **Theme:** Custom dark blue/purple theme

---

## 🚀 Getting Started

### 1. Clone the repo
```bash
git clone https://github.com/jinals-p/AndroidAIExamples.git
cd AndroidAIExamples
```

### 2. Open in Android Studio
- Open **Android Studio Hedgehog** or later
- Select `Open` → choose the cloned folder
- Wait for Gradle sync to complete

### 3. Add your Gemini API key
Modules 05 and 06 require a free Gemini API key:
- Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
- Sign in with Google → Create API key → Copy it
- Find the API key constant in the project and replace:
```kotlin
const val GEMINI_API_KEY = "YOUR_API_KEY_HERE"
```
> **Free tier:** 1,500 requests/day — no credit card needed

### 4. Run the app
- Connect your Android device or start an emulator
- Hit ▶️ **Run** in Android Studio
- The home screen lists all 10 modules — tap any card to open it
- Use the back button to return to the home screen

---

## 📦 Project Structure

```
AndroidAIExamples/
│
├── app/
│   └── MainActivity.kt                  → NavHost + HomeScreen + Screen routes
│
├── core/
│   ├── AppHeader.kt                     → Shared top bar component
│   ├── StatusBadge.kt                   → Shared badge component
│   └── theme/
│       ├── Color.kt                     → All color definitions
│       └── Theme.kt                     → Dark blue/purple theme
│
├── mlkitqrscanner/                      → Module 01
├── mlkitfacedetection/                  → Module 02
├── mlkittextrecognition/                → Module 03
├── mlkitimagelabeling/                  → Module 04
├── geminiphotoexplainer/                → Module 05
├── geminichatapp/                       → Module 06
├── geminivoice/                         → Module 07 (coming soon)
├── geminipdf/                           → Module 08 (coming soon)
├── tflitecustommodel/                   → Module 09 (coming soon)
└── completeaiapp/                       → Module 10 (coming soon)
```

---

## 🧭 Navigation

The app uses **Jetpack Navigation Compose** with a central `NavHost` in `MainActivity`.

```kotlin
// All available routes
Screen.Main           → "main_screen"            // Home screen
Screen.QrScanner      → "qr_scanner_screen"      // Module 01
Screen.FaceDetector   → "face_detector_screen"   // Module 02
Screen.TextDetector   → "text_detector_screen"   // Module 03
Screen.ImageLabeling  → "image_labeling_screen"  // Module 04
Screen.PhotoExplainer → "photo_explainer_screen" // Module 05
Screen.Chat           → "chat_screen"            // Module 06
```

Each screen receives an `onBackCallback` lambda to return home:
```kotlin
ScannerScreen(onBackCallback = {
    navController.navigate(Screen.Main.route)
})
```

---

## 🔍 Module Details

### 01 QR / Barcode Scanner
**Package:** `com.app.aiexamples.mlkitqrscanner`
**Tech:** ML Kit Barcode Scanning + CameraX

Scan QR codes and barcodes in real time. Runs fully **on-device** — no internet required.

**Features:**
- Supports QR Code, EAN-13, EAN-8, Code 128, UPC-A, PDF417, Aztec and more
- Animated scan frame with color change on detection
- Torch / flashlight toggle
- Result card slides up on successful scan
- Scan again without restarting camera
- Camera permission with rationale and Settings redirect

**Compose concepts:**
`AndroidView` · `Canvas` · `AnimatedVisibility` · `DisposableEffect` · `rememberPermissionState`

---

### 02 Face Detection
**Package:** `com.app.aiexamples.mlkitfacedetection`
**Tech:** ML Kit Face Detection + CameraX

Detect faces in real time with bounding boxes, smile detection, eye state tracking, head direction and facial contour points.

**Features:**
- Live bounding box drawn exactly over detected faces
- Smile probability with animated progress bar
- Left/right eye open or closed detection
- Head direction — looking left, right or forward
- Facial contour overlay with 36+ landmark points
- Multiple faces simultaneously
- Front camera with correct left/right mirror compensation

**Compose concepts:**
`Canvas drawing` · `animateColorAsState` · `animateFloatAsState` · `KeepScreenOn` · Front camera mirroring fix

---

### 03 Text Recognition
**Package:** `com.app.aiexamples.mlkittextrecognition`
**Tech:** ML Kit Text Recognition + CameraX

Read and extract printed or handwritten text from any real-world surface in real time.

**Features:**
- Live text detection from camera feed
- Freeze / capture — stop scanning to read result clearly
- Copy to clipboard with animated success badge
- Share text via any installed app
- Text block count display
- Torch toggle for low-light conditions

**Compose concepts:**
`scaleIn/scaleOut` animation · `verticalScroll` · `LaunchedEffect` with delay · Clipboard via `Context`

---

### 04 Image Labeling
**Package:** `com.app.aiexamples.mlkitimagelabeling`
**Tech:** ML Kit Image Labeling + CameraX

Identify and label objects, scenes and concepts in real time using your camera.

**Features:**
- Real-time object identification with up to 8 labels shown
- Emoji mapping for all recognized labels
- Adjustable confidence threshold slider (30%–95%)
- Color-coded chips by confidence level
- Animated confidence progress bar with gradient
- Horizontal scrollable label chips

**Compose concepts:**
`LazyRow` · `Slider` · `animateFloatAsState` · Horizontal gradient brush · Dynamic color theming

---

### 05 Photo Explainer
**Package:** `com.app.aiexamples.geminiphotoexplainer`
**Tech:** Gemini API (gemini-1.5-flash) — multimodal vision

Take a photo or pick from gallery and ask Gemini AI anything about it.

**Features:**
- Take photo with camera or pick from gallery
- Custom prompt input
- Quick prompt suggestion chips
- Gemini Vision processes image + text together
- Loading state with spinner
- Sealed class state management (Idle / Loading / Success / Error)

**Requires:** Free Gemini API key from [aistudio.google.com](https://aistudio.google.com/app/apikey)

**Try these prompts:**
```
"What is in this image?"
"Is this food healthy?"
"What brand is this product?"
"Translate any text visible in this image"
"Describe this for a visually impaired person"
```

**Compose concepts:**
`ActivityResultContracts` · `sealed class` state · `AnimatedVisibility` · `BitmapFactory`

---

### 06 AI Chat
**Package:** `com.app.aiexamples.geminichatapp`
**Tech:** Gemini API (gemini-1.5-flash) — streaming + chat history

A full AI chatbot with streaming responses word by word, conversation memory and a polished chat UI.

**Features:**
- Streaming responses — words appear one by one like ChatGPT
- Conversation memory across all messages in the session
- Animated typing indicator (3 bouncing dots)
- User and AI chat bubbles with different corner shapes
- Auto scroll to latest message
- Send via button or keyboard action
- Input disabled while AI is responding

**Requires:** Free Gemini API key from [aistudio.google.com](https://aistudio.google.com/app/apikey)

**Compose concepts:**
`LazyColumn` with `key` · `StateFlow` list updates · `sendMessageStream` · `weight(1f)` layout · Keyboard + `ImeAction.Send` handling · State hoisting

---

### 07 Voice Assistant
**Package:** `com.app.aiexamples.geminivoice`

> ⏳ Coming Soon

Speak to AI and hear it respond out loud. Uses Android built-in `SpeechRecognizer` and `TextToSpeech` with Gemini as the brain — no third-party libraries needed.

**Planned features:**
- Microphone pulse animation while listening
- Speech to text via Android `SpeechRecognizer`
- Gemini processes the spoken question
- AI response spoken aloud via `TextToSpeech`
- Full conversation history

---

### 08 PDF Summarizer
**Package:** `com.app.aiexamples.geminipdf`

> ⏳ Coming Soon

Pick any PDF document and get an AI-generated summary, key points and Q&A powered by Gemini.

**Planned features:**
- PDF file picker
- Page-by-page text extraction
- AI summary with key points
- Ask questions about the document

---

### 09 Custom AI Model
**Package:** `com.app.aiexamples.tflitecustommodel`

> ⏳ Coming Soon

Train a custom model in Python (Google Colab), export as `.tflite` and run it completely offline inside Android.

**Planned features:**
- Python training notebook included
- `.tflite` model bundled in assets
- Real-time inference on camera frames
- Runs 100% offline — no internet needed

---

### 10 Complete AI App
**Package:** `com.app.aiexamples.completeaiapp`

> ⏳ Coming Soon

A full production-ready AI app combining all previous modules with proper navigation, multi-screen MVVM architecture and a polished UI ready for Play Store.

---

## 🔑 API Keys

| Module | Key needed | Where to get | Cost |
|--------|-----------|--------------|------|
| 01 QR Scanner | ❌ None | — | Free |
| 02 Face Detection | ❌ None | — | Free |
| 03 Text Recognition | ❌ None | — | Free |
| 04 Image Labeling | ❌ None | — | Free |
| 05 Photo Explainer | ✅ Gemini | [aistudio.google.com](https://aistudio.google.com/app/apikey) | Free |
| 06 AI Chat | ✅ Gemini | [aistudio.google.com](https://aistudio.google.com/app/apikey) | Free |
| 07 Voice Assistant | ✅ Gemini | [aistudio.google.com](https://aistudio.google.com/app/apikey) | Free |
| 08 PDF Summarizer | ✅ Gemini | [aistudio.google.com](https://aistudio.google.com/app/apikey) | Free |
| 09 Custom Model | ❌ None | — | Free |
| 10 Complete App | ✅ Gemini | [aistudio.google.com](https://aistudio.google.com/app/apikey) | Free |

---

## 📚 What You Will Learn

**Jetpack Compose**
- `LazyColumn` / `LazyRow` with `key` for smooth animations
- `AnimatedVisibility` · `animateColorAsState` · `animateFloatAsState`
- `Canvas` drawing — bounding boxes, contours, scan overlays
- `AndroidView` — embedding CameraX `PreviewView` in Compose
- `StateFlow` + `collectAsStateWithLifecycle`
- Permission handling with Accompanist
- `DisposableEffect` · `LaunchedEffect`
- `ViewModel` + MVVM architecture
- `NavHost` + `NavController` navigation
- Keyboard and IME handling
- State hoisting pattern

**Android AI**
- ML Kit — on-device AI (no internet, no API key)
- CameraX — modern camera with `ImageAnalysis`
- Gemini API — multimodal AI (text + image + streaming)
- TensorFlow Lite — custom offline model inference
- Speech recognition + Text to speech
- Real-time frame-by-frame image analysis

---

## 🛠️ Requirements

- Android Studio Hedgehog (2023.1.1) or later
- Android device or emulator running API 24+
- Physical device recommended for camera features
- Internet connection required for Gemini modules (05–08)

---

## 🤝 Contributing

Contributions, issues and feature requests are welcome!

1. Fork the repo
2. Create your branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

```
MIT License — Copyright (c) 2025 jinals-p

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, subject to the condition
that the above copyright notice appears in all copies.
```

---

## ⭐ Support

If this repo helped you learn Android AI development, please give it a ⭐ star —
it helps other developers discover this resource!

[![GitHub stars](https://img.shields.io/github/stars/jinals-p/AndroidAIExamples?style=social)](https://github.com/jinals-p/AndroidAIExamples/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/jinals-p/AndroidAIExamples?style=social)](https://github.com/jinals-p/AndroidAIExamples/network/members)

---

<p align="center">Made with ❤️ for the Android developer community</p>