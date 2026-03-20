# HireReady

HireReady is an Android application that simulates real-world interview scenarios and provides structured, AI-driven feedback. It is designed to help students and job seekers improve communication, answer quality, and interview confidence through repeated practice and evaluation.

## Overview

Traditional interview preparation often lacks structured practice and actionable feedback. HireReady addresses this gap by combining guided mock interviews with automated evaluation. Users can answer role-based interview questions using voice or text, and receive a score along with detailed feedback highlighting strengths, weaknesses, and areas for improvement.

## Features

- Role-based interview question packs (e.g., HR, Sales, Customer Support)
- Voice input using Android Speech Recognition
- Text-based answer mode
- Timed interview simulation
- AI-based evaluation with score out of 100
- Structured feedback:
  - Strengths
  - Weaknesses
  - Improvement suggestions
- Firebase-backed data storage for interview sessions
- Extensible architecture for analytics and progress tracking



## System Architecture


Android Application (Java)
|
|-- SpeechRecognizer (voice to text)
|
|-- AIEvaluator (OpenAI API via HTTP)
|
|-- Firebase Firestore
| |-- users/{userId}/userSessions/{sessionId}
|
|-- FeedbackActivity (data visualization)


## How It Works

1. The user selects an interview question pack.
2. The application presents questions in sequence.
3. The user answers using voice or text input.
4. Voice input is converted into text using SpeechRecognizer.
5. All responses are combined and sent to the OpenAI API.
6. The AI evaluates the responses based on clarity, structure, and relevance.
7. The result (score and feedback) is stored in Firestore.
8. The Feedback screen retrieves and displays the evaluation.

---

## Technology Stack

### Frontend
- Android (Java)
- XML layouts with Material Design components

### Backend and Services
- Firebase Authentication
- Cloud Firestore

### AI Integration
- OpenAI API (gpt-4o-mini)
- OkHttp for HTTP requests

---

## Author

Muhammad Ibrahiem  
Software Engineering Student
