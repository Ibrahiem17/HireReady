package com.example.hireready;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotActivity extends AppCompatActivity {

    private EditText etPrompt;
    private Button btnSend;
    private RecyclerView rvChat;

    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    private final String apiKey = BuildConfig.GEMINI_API_KEY;

    private final String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        etPrompt = findViewById(R.id.etPrompt);
        btnSend = findViewById(R.id.btnSend);
        rvChat = findViewById(R.id.rvChat);

        adapter = new MessageAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        RequestQueue queue = Volley.newRequestQueue(this);

        btnSend.setOnClickListener(v -> {
            String userInput = etPrompt.getText().toString().trim();
            if (userInput.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add user message to chat
            addMessage(userInput, true);

            etPrompt.setText("");
            btnSend.setEnabled(false);

            try {
                // Construct JSON body for Gemini API
                JSONObject jsonBody = new JSONObject();
                JSONArray partsArray = new JSONArray();
                JSONObject partObj = new JSONObject();
                partObj.put("text", userInput);
                partsArray.put(partObj);

                JSONObject contentObj = new JSONObject();
                contentObj.put("parts", partsArray);

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(contentObj);

                jsonBody.put("contents", contentsArray);

                // Send POST request
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonBody,
                        response -> {
                            btnSend.setEnabled(true);
                            try {
                                JSONArray candidates = response.getJSONArray("candidates");
                                String reply = "No response";
                                if (candidates.length() > 0) {
                                    JSONObject firstCandidate = candidates.getJSONObject(0);
                                    JSONObject content = firstCandidate.getJSONObject("content");
                                    JSONArray parts = content.getJSONArray("parts");
                                    reply = parts.getJSONObject(0).getString("text");
                                }

                                // Add bot message to chat
                                addMessage(reply, false);

                            } catch (JSONException e) {
                                addMessage("Parsing Error: " + e.getMessage(), false);
                            }
                        },
                        error -> {
                            btnSend.setEnabled(true);
                            String errorMsg = "Network Error";
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                errorMsg = "API Error: " + error.networkResponse.statusCode;
                            }
                            addMessage(errorMsg, false);
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                request.setRetryPolicy(new DefaultRetryPolicy(
                        60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(request);

            } catch (JSONException e) {
                btnSend.setEnabled(true);
                addMessage("JSON Creation Error: " + e.getMessage(), false);
            }
        });
    }


    private void addMessage(String text, boolean isUser) {
        messageList.add(new Message(text, isUser));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }
}
