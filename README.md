# StudyPulse AI 🧠

StudyPulse AI is a modern, AI-powered study assistant designed to transform the way students learn. By leveraging Generative AI, it converts study notes and documents into interactive flashcards and quizzes, helping users achieve mastery faster.

## 🚀 Key Features

*   **AI Deck Generation**: Automatically generate study materials from raw text notes using Google Gemini AI.
*   **File Import Support**: Upload PDF, TXT, or DOCX documents to create decks instantly.
*   **Flashcard Mastery**: Track your retention with mastery levels (Struggling, Learning, Mastered).
*   **Interactive Quizzes**: Test your knowledge with AI-generated multiple-choice questions.
*   **Personalized Progress**: Visual analytics of your study activity, streaks, and subject-wise performance.
*   **Secure Cloud Sync**: Seamlessly sync your decks across devices using Supabase backend integration.
*   **Modern Material Design**: A clean, intuitive UI featuring Material 3 components, dark mode support, and smooth animations.

## 🛠️ Tech Stack

*   **Platform**: Android (Target SDK 36)
*   **Languages**: Java & Kotlin (Hybrid)
*   **Generative AI**: Google Gemini 1.5 Flash
*   **Local Database**: Room Persistence Library
*   **Backend as a Service**: Supabase (Postgrest, Auth, Storage)
*   **Networking**: OkHttp, Ktor, GSON
*   **UI/UX**: Material 3, Navigation Component, Glide
*   **File Processing**: PDFBox (PDF), Apache POI (DOCX)

## 📁 Folder Structure

```text
StudyPulse_AI/
├── app/
│   ├── src/main/
│   │   ├── java/com/yourgroup/studypulseai/
│   │   │   ├── data/
│   │   │   │   ├── db/          # Room database, TypeConverters, and DAOs
│   │   │   │   └── model/       # Data entities (Deck, Flashcard, QuizQuestion)
│   │   │   ├── network/
│   │   │   │   ├── models/      # Supabase DTOs
│   │   │   │   ├── SupabaseManager.kt  # Supabase client initialization
│   │   │   │   ├── SupabaseRepo.kt     # Repository for Supabase operations
│   │   │   │   └── GeminiApiService.java # AI generation logic
│   │   │   ├── ui/              # Fragment-based UI modules
│   │   │   │   ├── auth/        # Login, Registration, and Welcome flows
│   │   │   │   ├── home/        # Dashboard and Deck management
│   │   │   │   ├── newdeck/     # AI Deck creation and File Preview
│   │   │   │   ├── study/       # Flashcard session and Quiz logic
│   │   │   │   ├── progress/    # Learning analytics and Charts
│   │   │   │   └── settings/    # User preferences and Profile
│   │   │   ├── util/            # File extraction and Progress helpers
│   │   │   ├── MainActivity.java      # Single Activity host
│   │   │   └── StudyPulseApp.java     # Application class (Global init)
│   │   ├── res/
│   │   │   ├── layout/          # XML Layout definitions
│   │   │   ├── navigation/      # Jetpack Navigation Graph
│   │   │   ├── values/          # Strings, Colors, Themes (Light/Dark)
│   │   │   └── drawable/        # Vector assets and custom gradients
│   │   └── AndroidManifest.xml
│   └── build.gradle             # Module-level build script
├── gradle/
│   └── libs.versions.toml       # Version Catalog for dependencies
├── gradle.properties            # API Keys (GEMINI_API_KEY)
├── build.gradle                 # Project-level build script
└── README.md
```

## ⚙️ Setup Instructions

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/yourgroup/studypulse-ai.git
    ```
2.  **API Keys**:
    Add your Google Gemini API Key to `gradle.properties`:
    ```properties
    GEMINI_API_KEY=your_api_key_here
    ```
3.  **Supabase Configuration**:
    Update `SupabaseConfig.java` with your project URL and Anon Key.
4.  **Build & Run**:
    Open the project in Android Studio (Ladybug or newer) and run on a device with API 26+.

## 👥 Contributors
- O.P.S.Perera
- S.H.G.Mithila
- M.A.S.Karunaratne
