# StudyPulse AI 🧠

StudyPulse AI is a modern, AI-powered study assistant designed to transform the way students learn. By leveraging Generative AI, it converts study notes and documents into interactive flashcards and quizzes, helping users achieve mastery faster.

---

## 📄 Project Abstract

### 1. Project Description
StudyPulse AI is an innovative Android application designed to streamline the academic learning process by automating the creation of study materials. The app serves as a personalized study companion that bridges the gap between passive reading and active recall. By allowing users to upload various document formats (PDF, DOCX, TXT) or input raw notes, the system automatically generates structured study decks consisting of flashcards and multiple-choice quizzes, tailored to the user's specific academic level and goals.

### 2. Method Used
The application is built on a robust hybrid architecture utilizing **Java and Kotlin** for the Android frontend, following the **Single Activity** pattern with the **Jetpack Navigation Component**. 
- **Content Extraction**: Uses **PDFBox** and **Apache POI** for high-fidelity text extraction from documents.
- **Data Persistence**: Employs **Room Database** for local caching and offline access to study materials.
- **Backend Infrastructure**: Integrated with **Supabase** for secure user authentication (Auth), real-time database synchronization (Postgrest), and cloud storage.
- **Learning Algorithm**: Implements an active recall and mastery tracking system that categorizes cards based on user performance (Struggling, Learning, Mastered).

### 3. AI Contribution
The core intelligence of StudyPulse AI is powered by the **Google Gemini 1.5 Flash** Large Language Model (LLM). The AI's contribution is multi-faceted:
- **Automated Generation**: It analyzes extracted text to identify key concepts, definitions, and relationships, transforming them into concise Q&A pairs.
- **Context-Aware Assessment**: Generates pedagogically sound distractors for multiple-choice questions to ensure effective knowledge testing.
- **AI Pulse Check**: Analyzes user mastery patterns to provide personalized study advice, identifying knowledge gaps and recommending specific refresher sessions.

### 4. Novelty of the Proposed Solution
Unlike traditional flashcard apps that require manual entry, StudyPulse AI introduces:
- **Instant Document Transformation**: The ability to turn a 50-page PDF into a structured study session in seconds using Generative AI.
- **Adaptive Study Ecosystem**: A seamless integration between local persistence and cloud sync, ensuring a "zero-effort" setup for students.
- **Hybrid AI Advice**: Moving beyond simple statistics to offer actionable, AI-driven qualitative feedback through the "AI Pulse Check" feature, which identifies not just *what* was missed, but *why* a student might be struggling with a specific subject.

---

## 🚀 Key Features

*   **User Authentication**: Secure sign-up and login system using Supabase Auth, including a dedicated "Welcome" onboarding flow.
*   **AI Deck Generation**: Automatically generate study materials from raw text notes or uploaded documents using Google Gemini 1.5 Flash.
*   **File Import Support**: Support for PDF, TXT, and DOCX file formats for instant study material creation.
*   **Flashcard Mastery**: Active recall system with flipping animations and mastery tracking (Struggling, Learning, Mastered).
*   **Interactive Quizzes**: Test your knowledge with AI-generated multiple-choice questions and instant performance feedback.
*   **Personalized Progress**: Visual analytics with charts showing study activity, streaks, and subject-wise mastery.
*   **Profile Management**: Easily update your display name, change your password, and set personalized study goals.
*   **Modern Material Design**: A responsive UI with a dedicated Splash Screen, Dark Mode support, and Material 3 components.
*   **Secure Cloud Sync**: All your decks and progress are synced in real-time using Supabase backend integration.

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
- M.A.S.karunarthne

---

## 🎬 Video Demonstration Script (3-Minute Guide)

This script is designed for a concise 3-minute walkthrough of the StudyPulse AI application.

---

## 🎬 Video Demonstration Script (Comprehensive Guide)

This script is designed for a detailed walkthrough of the StudyPulse AI application, covering all core features and user flows.

### **Part 1: Introduction & Secure Onboarding**
*   **[Visual: App Launch]** Start with the app splash screen. Notice the sharp, theme-aware logo (`brain.jpg` for light, gradient for dark).
*   **[Visual: Welcome Screen]** Land on the "Get Started" page.
*   **[Narrator]** "Welcome to StudyPulse AI, your personal study companion. We've built an application that takes the heavy lifting out of creating study materials."
*   **[Visual: Registration]** Click 'Sign Up'. Show the registration form (Name, Email, Password). 
*   **[Narrator]** "Getting started is seamless. Our registration system, powered by Supabase Auth, ensures your data is secure and synced across all your devices."
*   **[Visual: Dashboard]** Log in/Register to see the main dashboard with personalized time-based greetings.

### **Part 2: AI-Powered Content Creation**
*   **[Visual: New Deck Fragment]** Tap the '+' icon in the bottom navigation.
*   **[Narrator]** "The core of StudyPulse is its ability to learn with you. In the 'New Deck' section, you can upload PDFs, Word documents, or text notes."
*   **[Visual: File Selection]** Briefly show a PDF being selected and processed.
*   **[Narrator]** "Using Google Gemini 1.5 Flash, StudyPulse extracts the most important concepts and automatically generates a complete deck of flashcards and quizzes in seconds."

### **Part 3: The Learning Experience (Flashcards & Quizzes)**
*   **[Visual: Study Fragment]** Open a deck and show the Question.
*   **[Visual: Card Flip]** Tap the card to show the "Answer" with a smooth flipping animation.
*   **[Visual: Mastery Selection]** Select 'Mastered' or 'Struggling' on the card.
*   **[Narrator]** "Our study mode uses active recall. You can flip cards to test yourself and rate your mastery. This feedback is crucial for tracking your long-term retention."
*   **[Visual: Quiz Fragment]** Switch to the 'Quiz' tab and answer an AI-generated multiple-choice question.
*   **[Narrator]** "To truly validate your knowledge, StudyPulse generates interactive quizzes. These MCQs provide instant feedback, turning static notes into an active learning session."
*   **[Visual: Finish]** Reach the last card and click the green 'Finish' button to navigate back home.

### **Part 4: Deep-Dive: Progress & Analytics**
*   **[Visual: Progress Fragment]** Navigate to the 'Progress' tab.
*   **[Visual: Mastery Charts]** Point out the 'Flashcard Mastery' donut chart and the 'Quiz Performance' metrics.
*   **[Narrator]** "Data-driven learning is at our heart. The Progress page provides a comprehensive breakdown of your journey. You can see exactly how many cards you've mastered versus those you're still learning."
*   **[Visual: Weekly Pulse]** Scroll to the 'Weekly Pulse' bar chart showing study activity.
*   **[Narrator]** "The Weekly Pulse tracks your consistency, helping you maintain study streaks. We also provide 'Subject-Wise Progress' so you know exactly which modules need more attention."
*   **[Visual: AI Pulse Advice]** Highlight the 'AI Pulse Check' card at the bottom.
*   **[Narrator]** "Our 'AI Pulse Check' goes a step further by offering personalized advice, identifying your weakest subjects and suggesting refresher sessions to optimize your study time."

### **Part 5: Personalization & Account Management**
*   **[Visual: Settings Fragment]** Navigate to the 'Settings' tab.
*   **[Visual: Edit Profile]** Click 'Edit Profile Name'. Change the name in the dialog and save.
*   **[Narrator]** "StudyPulse is designed to be yours. You can easily update your profile information, such as your display name, which immediately updates your personalized greetings."
*   **[Visual: Change Password]** Click 'Change Password' to show the secure update dialog.
*   **[Visual: Study Goals]** Adjust the 'Daily Study Goal' slider.
*   **[Narrator]** "You can also set daily study goals and update your academic level. These preferences help the AI tailor the complexity of generated content to your needs."
*   **[Visual: Dark Mode Toggle]** Toggle **Dark Mode** on/off to show the UI adaptation.
*   **[Narrator]** "Finally, whether you prefer a clean Light interface or a sleek Dark Mode, StudyPulse adapts to your environment. StudyPulse AI: Study smarter, not harder."

---

Created by **MC Group Project Team**.
