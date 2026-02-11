package com.example.hireready;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockInterviewActivity extends AppCompatActivity {

    private static final String TAG = "MockInterviewActivity";

    private TextView questionText, methodPromptText;
    private Chronometer timer;
    private ImageButton recordButton;
    private Button submitButton, nextButton, prevButton, voiceAnswerButton, textAnswerButton;
    private LinearLayout answerOptions, interviewUiContainer;
    private TextInputLayout textAnswerLayout;
    private EditText textAnswerInput;

    private List<String> questions;
    private List<String> textAnswers;
    private int currentQuestionIndex = 0;
    private String packTitle;
    private boolean isVoiceMode = false;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_interview);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        methodPromptText = findViewById(R.id.method_prompt_text);
        questionText = findViewById(R.id.question_text);
        timer = findViewById(R.id.timer);
        recordButton = findViewById(R.id.record_button);
        submitButton = findViewById(R.id.submit_button);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);
        voiceAnswerButton = findViewById(R.id.voice_answer_button);
        textAnswerButton = findViewById(R.id.text_answer_button);
        answerOptions = findViewById(R.id.answer_options);
        interviewUiContainer = findViewById(R.id.interview_ui_container);
        textAnswerLayout = findViewById(R.id.text_answer_layout);
        textAnswerInput = findViewById(R.id.text_answer_input);

        questions = getIntent().getStringArrayListExtra("questions");
        packTitle = getIntent().getStringExtra("packTitle");

        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "No questions found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textAnswers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            textAnswers.add("");
        }

        voiceAnswerButton.setOnClickListener(v -> {
            isVoiceMode = true;
            startInterview();
        });

        textAnswerButton.setOnClickListener(v -> {
            isVoiceMode = false;
            startInterview();
        });

        recordButton.setOnClickListener(v -> {
            if (!isListening) startListening();
            else stopListening();
        });

        nextButton.setOnClickListener(v -> {
            saveTextAnswer();
            currentQuestionIndex++;
            updateQuestion();
        });

        prevButton.setOnClickListener(v -> {
            saveTextAnswer();
            currentQuestionIndex--;
            updateQuestion();
        });

        submitButton.setOnClickListener(v -> {
            saveTextAnswer();
            saveSessionAndRunAI();
        });
    }

    private void startInterview() {
        methodPromptText.setVisibility(View.GONE);
        answerOptions.setVisibility(View.GONE);
        interviewUiContainer.setVisibility(View.VISIBLE);

        if (isVoiceMode) {
            recordButton.setVisibility(View.VISIBLE);
            timer.setVisibility(View.VISIBLE);
            setupSpeechRecognizer();
        } else {
            textAnswerLayout.setVisibility(View.VISIBLE);
        }
        updateQuestion();
    }

    private void saveTextAnswer() {
        textAnswers.set(currentQuestionIndex, textAnswerInput.getText().toString());
    }

    private void updateQuestion() {
        questionText.setText(questions.get(currentQuestionIndex));
        textAnswerInput.setText(textAnswers.get(currentQuestionIndex));

        prevButton.setVisibility(currentQuestionIndex == 0 ? View.GONE : View.VISIBLE);
        nextButton.setVisibility(currentQuestionIndex < questions.size() - 1 ? View.VISIBLE : View.GONE);
        submitButton.setVisibility(currentQuestionIndex == questions.size() - 1 ? View.VISIBLE : View.GONE);
    }

    private void setupSpeechRecognizer() {
        if (speechRecognizer != null) return;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            public void onReadyForSpeech(Bundle params) {
                isListening = true;
                recordButton.setImageResource(R.drawable.ic_stop);
            }

            public void onBeginningOfSpeech() {
                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();
            }

            public void onEndOfSpeech() {
                timer.stop();
                isListening = false;
                recordButton.setImageResource(R.drawable.ic_mic);
            }

            public void onResults(Bundle results) {
                ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    textAnswerInput.setText(matches.get(0));
                }
            }

            public void onError(int error) {
                isListening = false;
                recordButton.setImageResource(R.drawable.ic_mic);
                Toast.makeText(MockInterviewActivity.this, getErrorText(error), Toast.LENGTH_LONG).show();
            }

            public void onRmsChanged(float rmsdB) {}
            public void onBufferReceived(byte[] buffer) {}
            public void onPartialResults(Bundle partialResults) {}
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    private void stopListening() {
        speechRecognizer.stopListening();
    }

    private void saveSessionAndRunAI() {
        if (currentUser == null) return;

        Map<String, Object> session = new HashMap<>();
        session.put("packTitle", packTitle);
        session.put("answers", textAnswers);
        session.put("questions", questions);
        session.put("evaluationType", "AI_PENDING");
        session.put("timeStamp", FieldValue.serverTimestamp());

        db.collection("users")
                .document(currentUser.getUid())
                .collection("userSessions")
                .add(session)
                .addOnSuccessListener(doc -> runAIEvaluation(doc.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving session", e));
    }

    private void runAIEvaluation(String sessionId) {

        StringBuilder combinedAnswers = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            combinedAnswers.append("Q: ").append(questions.get(i)).append("\n");
            combinedAnswers.append("A: ").append(textAnswers.get(i)).append("\n\n");
        }

        AIEvaluator.evaluate(
                packTitle,
                combinedAnswers.toString(),
                new AIEvaluator.EvaluationCallback() {

                    @Override
                    public void onSuccess(JSONObject result) {
                        try {
                            Map<String, Object> update = new HashMap<>();
                            update.put("score", result.getInt("score"));
                            update.put("evaluationType", "AI");

                            Map<String, Object> aiFeedback = new HashMap<>();
                            aiFeedback.put("strengths", jsonArrayToList(result.getJSONArray("strengths")));
                            aiFeedback.put("weaknesses", jsonArrayToList(result.getJSONArray("weaknesses")));
                            aiFeedback.put("improvements", jsonArrayToList(result.getJSONArray("improvements")));

                            update.put("aiFeedback", aiFeedback);

                            db.collection("users")
                                    .document(currentUser.getUid())
                                    .collection("userSessions")
                                    .document(sessionId)
                                    .update(update)
                                    .addOnCompleteListener(task -> {
                                        if (!task.isSuccessful()) {
                                            Log.w(TAG, "Error updating document", task.getException());
                                        }
                                        navigateToFeedback(sessionId);
                                    });

                        } catch (Exception e) {
                            Log.e(TAG, "AI parse error", e);
                            navigateToFeedback(sessionId);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "AI evaluation failed: " + error);
                        navigateToFeedback(sessionId);
                    }
                }
        );
    }

    private void navigateToFeedback(String sessionId) {
        Intent i = new Intent(MockInterviewActivity.this, FeedbackActivity.class);
        i.putExtra("sessionId", sessionId);
        startActivity(i);
        finish();
    }

    private List<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }

    public static String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error.";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client error.";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Microphone permission missing.";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error.";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout.";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech recognized.";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognizer busy.";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error.";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech detected.";
            default:
                return "Unknown error.";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
    }
}
