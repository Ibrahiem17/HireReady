# HireReady

HireReady is an Android application that simulates real interview scenarios and provides AI-driven feedback to help users improve communication, structure, and confidence.

---

## Features

- Role-based interview question packs  
- Voice (Speech Recognition) and text input modes  
- Timed interview simulation  
- AI evaluation with score out of 100 (OpenAI)  
- Structured feedback: strengths, weaknesses, improvements  
- Integrated chatbot for guidance and learning (Gemini API)  
- Firebase-backed session storage  

---

## Architecture
Android App (Java)
|
|-- SpeechRecognizer (voice to text)
|-- OpenAI (interview evaluation)
|-- Gemini API (chatbot)
|-- Firebase Firestore (data storage)


---

## How It Works

1. User selects an interview pack  
2. Answers questions using voice or text  
3. Responses are processed and sent for AI evaluation  
4. Score and feedback are generated and stored in Firestore  
5. Feedback is displayed in the app  

---

## Tech Stack

- Android (Java, XML)  
- Firebase Authentication & Firestore  
- OpenAI API (evaluation)  
- Gemini API (chatbot)  
- OkHttp  

---

## Author

Muhammad Ibrahiem  
Software Engineering Student
