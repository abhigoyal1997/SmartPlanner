package com.example.abhinav.smartplanner;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
    private CollectionReference dbEvents;
    private CollectionReference dbTasks;

    public void init(String uid) {
        dbChat = FirebaseFirestore.getInstance().collection("users").document(uid).collection("chat");
        dbCourses = FirebaseFirestore.getInstance().collection("users").document(uid).collection("courses");
        dbEvents = FirebaseFirestore.getInstance().collection("users").document(uid).collection("events");
        dbTasks = FirebaseFirestore.getInstance().collection("users").document(uid).collection("tasks");
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

//    public void addEvent(final CalEvent c, final boolean ignoreClash, final OnResponseListener responseListener) {
//        if (ignoreClash) {
//            addEvent(c, responseListener);
//            return;
//        }

//        dbEvents.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                final JSONObject response = new JSONObject();
//                try {
//                    if (task.isSuccessful()) {
//                        QuerySnapshot snapshot = task.getResult();
//                        if (snapshot != null && !snapshot.isEmpty()) {
//                            for (QueryDocumentSnapshot doc : snapshot) {
//                                if (isClash(c, doc.toObject(CalEvent.class))) {
//                                    response.put(STATUS, STATUS_OK);
//                                    response.put(DATA, false);
//                                    responseListener.onResponse(response);
//                                    return;
//                                }
//                            }
//                        }
//                        addEvent(c, responseListener);
//                    } else {
//                        response.put(STATUS, STATUS_ERROR);
//                        response.put(DATA, R.string.error_toast);
//                        responseListener.onResponse(response);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    public void addEvent(final CalEvent c, final OnResponseListener responseListener) {
        if (c.courseCode != null && !c.courseCode.equals("")) {
            dbCourses.document(c.courseCode).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    final JSONObject response = new JSONObject();
                    try {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc != null && doc.exists()) {
                                addEventToDb(c, responseListener);
                            } else {
                                response.put(STATUS, STATUS_OK);
                                response.put(DATA, false);
                                responseListener.onResponse(response);
                            }
                        } else {
                            response.put(STATUS, STATUS_ERROR);
                            response.put(DATA, R.string.error_toast);
                            responseListener.onResponse(response);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            addEventToDb(c, responseListener);
        }
    }

    public void getEvents(long from, long to, final OnResponseListener responseListener) {
        Query query = dbEvents.whereEqualTo("recur", false);
        if (to > from) {
            query = dbEvents.whereEqualTo("recur", false)
                    .whereGreaterThan("date", from)
                    .whereLessThan("date", to)
                    .orderBy("date");
            Log.d("get events", String.valueOf(to) + " " + from);
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                JSONObject response = new JSONObject();
                try {
                    if (task.isSuccessful()) {
                        List<CalEvent> calEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            calEvents.add(doc.toObject(CalEvent.class));
                        }
                        response.put(STATUS, STATUS_OK);
                        response.put(DATA, calEvents);
                    } else {
                        Log.d("get events", task.getException().toString());
                        response.put(STATUS, STATUS_ERROR);
                        response.put(DATA, R.string.error_toast);
                    }
                    Log.d("get-events", response.toString());
                    responseListener.onResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void addToDoTask(final ToDoTask toDoTask, final OnResponseListener responseListener) {
        dbTasks.add(toDoTask).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                final JSONObject response = new JSONObject();
                try {
                    if (task.isSuccessful()) {
                        response.put(STATUS, STATUS_OK);
                        response.put(DATA, true);
                        responseListener.onResponse(response);
                    } else {
                        response.put(STATUS, STATUS_ERROR);
                        response.put(DATA, R.string.error_toast);
                        responseListener.onResponse(response);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getSchedule(String courseCode, final OnResponseListener responseListener) {
        dbEvents.whereEqualTo("courseCode", courseCode).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                JSONObject response = new JSONObject();
                try {
                    if (task.isSuccessful()) {
                        List<CalEvent> calEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            calEvents.add(doc.toObject(CalEvent.class));
                        }
                        response.put(STATUS, STATUS_OK);
                        response.put(DATA, calEvents);
                    } else {
                        Log.d("get events", task.getException().toString());
                        response.put(STATUS, STATUS_ERROR);
                        response.put(DATA, R.string.error_toast);
                    }
                    Log.d("get-events", response.toString());
                    responseListener.onResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addEventToDb(CalEvent c, final OnResponseListener responseListener) {
        dbEvents.add(c).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                final JSONObject response = new JSONObject();
                try {
                    if (task.isSuccessful()) {
                        response.put(STATUS, STATUS_OK);
                        response.put(DATA, true);
                        responseListener.onResponse(response);
                    } else {
                        response.put(STATUS, STATUS_ERROR);
                        response.put(DATA, R.string.error_toast);
                        responseListener.onResponse(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}