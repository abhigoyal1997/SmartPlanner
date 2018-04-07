package com.example.abhinav.smartplanner;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.gson.JsonElement;

import java.lang.ref.WeakReference;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

/**
 * Created by abhi on 7/4/18.
 */

public class VirtualAssistant implements AIListener {

    private final String API_TAG = "ai api";

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

    public void handleQuery(String query) {
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(query);
        new QueryHandlerTask(this).execute(aiRequest);
    }

    private static class QueryHandlerTask extends AsyncTask<AIRequest, Void, AIResponse> {

        private WeakReference<VirtualAssistant> assistantWeakReference;

        QueryHandlerTask(VirtualAssistant assistant) {
            assistantWeakReference = new WeakReference<VirtualAssistant>(assistant);
        }

        @Override
        protected AIResponse doInBackground(AIRequest... aiRequests) {
            final AIRequest request = aiRequests[0];
            try {
                VirtualAssistant assistant = assistantWeakReference.get();
                return assistant.aiDataService.request(request);
            } catch (AIServiceException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(AIResponse response) {
            super.onPostExecute(response);
            VirtualAssistant assistant = assistantWeakReference.get();
            if (response != null) {
                Result result = response.getResult();
                Log.d(assistant.API_TAG, result.getFulfillment().getSpeech());
            }
        }
    }
}
