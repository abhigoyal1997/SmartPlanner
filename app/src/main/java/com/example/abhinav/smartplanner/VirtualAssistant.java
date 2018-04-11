package com.example.abhinav.smartplanner;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.ResponseMessage;
import ai.api.model.Result;

import static com.example.abhinav.smartplanner.Constants.*;

/**
 * Created by abhi on 7/4/18.
 */

public class VirtualAssistant implements AIListener {

    private final String API_TAG = "ai api";
    private static final String queryUrl = "https://api.dialogflow.com/v1/query?v=20150910";

    private AIService aiService;
    private AIDataService aiDataService;

    public VirtualAssistant() {
        String AI_ACCESS_TOKEN = "cc87191fa725498285c56d55fa0f3b2f";
        final AIConfiguration config = new AIConfiguration(AI_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        Log.d(API_TAG, String.valueOf(App.get()));
        aiService = AIService.getService(App.get(), config);
        aiService.setListener(this);

        aiDataService = new AIDataService(config);
    }

    public void startListening() {
        aiService.startListening();
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {
        Result result = response.getResult();

        // Get parameters
        StringBuilder parameterString = new StringBuilder();
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString.append("(").append(entry.getKey()).append(", ").append(entry.getValue()).append(") ");
            }
        }

        Log.d(API_TAG, parameterString.toString());
    }

    @Override
    public void onError(ai.api.model.AIError error) {
        Log.d(API_TAG, error.toString());
    }


    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    public void handleQuery(String query, OnResponseListener responseListener) {
        AIRequest request = new AIRequest(query);
        new QueryHandlerTask(this, responseListener).execute(request);
    }

    private static class QueryHandlerTask extends AsyncTask<AIRequest, Void, AIResponse> {

        private WeakReference<VirtualAssistant> assistantWeakRef;
        private WeakReference<OnResponseListener> responseListenerWeakRef;

        QueryHandlerTask(VirtualAssistant assistant, OnResponseListener responseListener) {
            assistantWeakRef = new WeakReference<>(assistant);
            responseListenerWeakRef = new WeakReference<>(responseListener);
        }

        @Override
        protected AIResponse doInBackground(AIRequest... aiRequests) {
            final AIRequest request = aiRequests[0];
            try {
                VirtualAssistant assistant = assistantWeakRef.get();
                return assistant.aiDataService.request(request);
            } catch (AIServiceException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(AIResponse response) {
            super.onPostExecute(response);
            JSONObject res = new JSONObject();
            try {
                if (response != null) {
                    Result result = response.getResult();
                    Log.d("result", result.getFulfillment().toString());
                    if (result.getFulfillment().getMessages().isEmpty()) {
                        res.put(STATUS, STATUS_ERROR);
                        res.put(DATA, R.string.error_toast);
                    } else {
                        ResponseMessage message = result.getFulfillment().getMessages().get(0);
                        res.put(STATUS, STATUS_OK);
                        if (message instanceof ResponseMessage.ResponsePayload) {
                            ResponseMessage.ResponsePayload payload = (ResponseMessage.ResponsePayload) message;
                            res.put(TYPE, TYPE_JSON);
                            res.put(DATA, new JSONObject(payload.getPayload().toString()));
                        } else if (message instanceof ResponseMessage.ResponseSpeech) {
                            ResponseMessage.ResponseSpeech speech = (ResponseMessage.ResponseSpeech) message;
                            res.put(TYPE, TYPE_TEXT);
                            res.put(DATA, speech.getSpeech().get(0));
                        }
                    }
                } else {
                    res.put(STATUS, STATUS_ERROR);
                    res.put(DATA, R.string.error_toast);
                }
                responseListenerWeakRef.get().onResponse(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
