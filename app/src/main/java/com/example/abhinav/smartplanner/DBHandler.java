package com.example.abhinav.smartplanner;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.abhinav.smartplanner.Constants.DATA;
import static com.example.abhinav.smartplanner.Constants.STATUS;
import static com.example.abhinav.smartplanner.Constants.STATUS_ERROR;
import static com.example.abhinav.smartplanner.Constants.STATUS_OK;

/**
 * Created by abhi on 10/4/18.
 */

public class DBHandler {
    private static final DBHandler mInstance = new DBHandler();

    private CollectionReference dbChat;
    private CollectionReference dbCourses;
    private CollectionReference dbClasses;

    public void init(String uid) {
        dbChat = FirebaseFirestore.getInstance().collection("users").document(uid).collection("chat");
        dbCourses = FirebaseFirestore.getInstance().collection("users").document(uid).collection("courses");
        dbClasses = FirebaseFirestore.getInstance().collection("users").document(uid).collection("classes");
    }

    public static DBHandler getInstance() {
        return mInstance;
    }

    public void addChat(ChatMessage message) {
        dbChat.add(message);
    }

    public void addChat(final ChatMessage message, final OnResponseListener responseListener) {
        Log.d("add-chat", message.getText());
        dbChat.add(message).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                JSONObject response = new JSONObject();
                try {
                    if (task.isSuccessful()) {
                        response.put(STATUS, STATUS_OK);
                        response.put(DATA, message.getText());
                    } else {
                        response.put(STATUS, STATUS_ERROR);
                        response.put(DATA, R.string.error_toast);
                    }
                    Log.d("chat-response", response.toString());
                    responseListener.onResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getCourseList(final OnResponseListener responseListener) {
        dbCourses.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                JSONObject response = new JSONObject();
                try {
                    if (task.isSuccessful()) {
                        List<String> courses = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            courses.add(doc.getId());
                        }
                        response.put(STATUS, STATUS_OK);
                        response.put(DATA, courses);
                    } else {
                        response.put(STATUS, STATUS_ERROR);
                        response.put(DATA, R.string.error_toast);
                    }
                    Log.d("get-course", response.toString());
                    responseListener.onResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addCourse(final String courseCode, final OnResponseListener responseListener) {
        final DocumentReference course = dbCourses.document(courseCode);
        course.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                final JSONObject response = new JSONObject();
                try {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            response.put(STATUS, STATUS_OK);
                            response.put(DATA, false);
                            responseListener.onResponse(response);
                        } else {
                            Map<String, String> data = new HashMap<>();
                            data.put("code", courseCode);
                            course.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        try {
                                            if (task.isSuccessful()) {
                                                response.put(STATUS, STATUS_OK);
                                                response.put(DATA, true);
                                            } else {
                                                response.put(STATUS, STATUS_ERROR);
                                                response.put(DATA, R.string.error_toast);
                                            }
                                            responseListener.onResponse(response);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        response.put(STATUS, STATUS_ERROR);
                        response.put(DATA, R.string.error_toast);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addClass(final ClassEvent c, final OnResponseListener responseListener) {
//        try {
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    public void addToDoTask(final ToDoTask toDoTask, final OnResponseListener responseListener){

    }
}
