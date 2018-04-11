package com.example.abhinav.smartplanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.helpers.LocatorImpl;

import java.lang.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.example.abhinav.smartplanner.Constants.DATA;
import static com.example.abhinav.smartplanner.Constants.NAME;
import static com.example.abhinav.smartplanner.Constants.PARAMS;
import static com.example.abhinav.smartplanner.Constants.QUERY;
import static com.example.abhinav.smartplanner.Constants.QUERY_ADD;
import static com.example.abhinav.smartplanner.Constants.QUERY_GET;
import static com.example.abhinav.smartplanner.Constants.SPEECH;
import static com.example.abhinav.smartplanner.Constants.SPEECH_DUP;
import static com.example.abhinav.smartplanner.Constants.SPEECH_EMP;
import static com.example.abhinav.smartplanner.Constants.SPEECH_NEG;
import static com.example.abhinav.smartplanner.Constants.SPEECH_POS;
import static com.example.abhinav.smartplanner.Constants.SPEECH_WAIT;
import static com.example.abhinav.smartplanner.Constants.STATUS;
import static com.example.abhinav.smartplanner.Constants.STATUS_OK;
import static com.example.abhinav.smartplanner.Constants.TYPE;
import static com.example.abhinav.smartplanner.Constants.TYPE_JSON;
import static com.example.abhinav.smartplanner.Constants.TYPE_TEXT;
import static com.example.abhinav.smartplanner.Constants.VALID;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class VAFragment extends Fragment {

    private final int RECORD_PERMISSION_REQUEST = 99;

    private VirtualAssistant assistant;

    private RecyclerView chatView;
    private EditText newChatMsg;
    private FirestoreRecyclerAdapter<ChatMessage, ChatViewHolder> adapter;
    private ProgressBar progressBar;

    private DBHandler dbHandler;
    private OnResponseListener aiResponseListener;

    FirestoreRecyclerOptions<ChatMessage> options;
    String uid;

    private boolean flagFab = true;

    public VAFragment() {
    }

    public static VAFragment newInstance() {
        return new VAFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View r = inflater.inflate(R.layout.fragment_va, container, false);
        assistant = new VirtualAssistant();

        chatView = r.findViewById(R.id.chat_recycler_view);
        progressBar = r.findViewById(R.id.loading_progress);
        newChatMsg = r.findViewById(R.id.chat_msg);
        RelativeLayout chatSendView = r.findViewById(R.id.chat_send);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        chatView.setLayoutManager(linearLayoutManager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user != null ? user.getUid() : "";

        dbHandler = DBHandler.getInstance();
        dbHandler.init(uid);

        aiResponseListener = new OnResponseListener() {
            @Override
            public void onResponse(JSONObject response) throws JSONException {
                if (response.getInt(STATUS) == STATUS_OK) {
                    if (response.getInt(TYPE) == TYPE_TEXT) {
                        updateChat(response.getString(DATA), "bot");
                    } else if (response.getInt(TYPE) == TYPE_JSON) {
                        handleRequest(response.getJSONObject(DATA));
                    }
                }
            }
        };

        chatSendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = newChatMsg.getText().toString().trim();
                if (!message.equals("")) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = null;
                    if (connectivityManager != null) {
                        networkInfo = connectivityManager.getActiveNetworkInfo();
                    }
                    if (networkInfo == null || !networkInfo.isConnected()) {
                        Toast.makeText(getContext(), "Not connected to internet!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateChat(message, "user");
                } else {
                    handleVoiceQuery();
                }
            }
        });

        newChatMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = r.findViewById(R.id.fab_img);
                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_send_white);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mic_white);


                if (s.toString().trim().length() != 0 && flagFab) {
                    imageViewAnimatedChange(getContext(), fab_img, img);
                    flagFab = false;

                } else if (s.toString().trim().length() == 0) {
                    imageViewAnimatedChange(getContext(), fab_img, img1);
                    flagFab = true;

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Query query = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("chat").orderBy("timestamp");

        options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class).build();

        adapter = new FirestoreRecyclerAdapter<ChatMessage, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder viewHolder, int position, @NonNull ChatMessage model) {
                if (model.getSender().equals("user")) {
                    viewHolder.rightText.setText(model.getText());
                    viewHolder.rightText.setVisibility(View.VISIBLE);
                    viewHolder.leftText.setVisibility(View.GONE);
                } else {
                    viewHolder.leftText.setText(model.getText());
                    viewHolder.leftText.setVisibility(View.VISIBLE);
                    viewHolder.rightText.setVisibility(View.GONE);
                }
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_chat, parent, false);
                return new ChatViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                progressBar.setVisibility(View.INVISIBLE);
                chatView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Toast.makeText(getContext(), "There was a problem while loading the chat!", Toast.LENGTH_SHORT).show();
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    chatView.scrollToPosition(positionStart);
                }
            }
        });

        chatView.setAdapter(adapter);

        return r;
    }

    private void handleRequest(final JSONObject request) {
        Log.d("request", request.toString());
        try {
            JSONObject query = request.getJSONObject(QUERY);
            final JSONObject speech = request.getJSONObject(SPEECH);
            final Random r = new Random();
            if (!query.getBoolean(VALID)) {
                Log.d("query", request.toString());
                JSONArray res = speech.getJSONArray(SPEECH_POS);
                updateChat(res.getString(r.nextInt(res.length())), "bot");
                return;
            }

            JSONArray wait = speech.getJSONArray(SPEECH_WAIT);
            updateChat(wait.getString(r.nextInt(wait.length() - 1)), "bot");
            if (query.getString(TYPE).equals(QUERY_GET)) {
                if (query.getString(NAME).equals("course")) {
                    dbHandler.getCourseList(new OnResponseListener() {
                        @Override
                        public void onResponse(JSONObject response) throws JSONException {
                            if (response.getInt(STATUS) == STATUS_OK) {
                                @SuppressWarnings("unchecked")
                                List<String> courses = (List<String>) response.get(DATA);
                                if (courses.isEmpty()) {
                                    JSONArray res = speech.getJSONArray(SPEECH_EMP);
                                    updateChat(res.getString(r.nextInt(res.length())), "bot");
                                } else {
                                    JSONArray res = speech.getJSONArray(SPEECH_POS);
                                    StringBuilder builder = new StringBuilder();
                                    for (String course : courses) {
                                        builder.append(course);
                                        builder.append('\n');
                                    }
                                    builder.deleteCharAt(builder.lastIndexOf("\n"));
                                    updateChat(res.getString(r.nextInt(res.length())) + '\n' + builder.toString(), "bot");
                                }
                            } else {
                                JSONArray res = speech.getJSONArray(SPEECH_NEG);
                                updateChat(res.getString(r.nextInt(res.length())), "bot");
                            }
                        }
                    });
                }
            } else if (query.getString(TYPE).equals(QUERY_ADD)) {
                if (query.getString(NAME).equals("course")) {
                    String courseCode = request.getJSONObject(PARAMS).getString("courseCode");
                    dbHandler.addCourse(courseCode, new OnResponseListener() {
                        @Override
                        public void onResponse(JSONObject response) throws JSONException {
                            JSONArray res;
                            if (response.getInt(STATUS) == STATUS_OK) {
                                if (response.getBoolean(DATA)) {
                                    res = speech.getJSONArray(SPEECH_POS);
                                } else {
                                    res = speech.getJSONArray(SPEECH_DUP);
                                }
                            } else {
                                res = speech.getJSONArray(SPEECH_NEG);
                            }
                            updateChat(res.getString(r.nextInt(res.length())), "bot");
                        }
                    });
                } else if (query.getString(NAME).equals("class")) {
                    JSONObject params = request.getJSONObject(PARAMS);
                    dbHandler.addEvent(new Event(params, Event.EVENT_CLASS), new OnResponseListener() {
                        @Override
                        public void onResponse(JSONObject response) throws JSONException {
                            JSONArray res;
                            if (response.getInt(STATUS) == STATUS_OK) {
                                if (response.getBoolean(DATA)) {
                                    res = speech.getJSONArray(SPEECH_POS);
                                } else {
                                    res = speech.getJSONArray(SPEECH_DUP);
                                }
                            } else {
                                res = speech.getJSONArray(SPEECH_NEG);
                            }
                            updateChat(res.getString(r.nextInt(res.length())), "bot");
                        }
                    });
                } else if (query.getString(NAME).equals("event")) {
                    JSONObject params = request.getJSONObject(PARAMS);
                    dbHandler.addEvent(new Event(params, Event.EVENT_CLASS), new OnResponseListener() {
                        @Override
                        public void onResponse(JSONObject response) throws JSONException {
                            JSONArray res;
                            if (response.getInt(STATUS) == STATUS_OK) {
                                if (response.getBoolean(DATA)) {
                                    res = speech.getJSONArray(SPEECH_POS);
                                } else {
                                    res = speech.getJSONArray(SPEECH_DUP);
                                }
                            } else {
                                res = speech.getJSONArray(SPEECH_NEG);
                            }
                            updateChat(res.getString(r.nextInt(res.length())), "bot");
                        }
                    });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateChat(String message, String sender) {
        ChatMessage chatMessage = new ChatMessage(message, sender, new Date());
        if (Objects.equals(sender, "user")) {
            dbHandler.addChat(chatMessage, new OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) throws JSONException {
                    if (response.getInt(STATUS) == STATUS_OK) {
                        String data = response.getString(DATA);
                        if (data.split("-").length < 5 && data.toLowerCase().contains("abort")) {
                            assistant.reset(aiResponseListener);
                        } else {
                            assistant.handleQuery(data, aiResponseListener);
                        }
                    } else {
                        Toast.makeText(getContext(), response.getString(DATA), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            newChatMsg.setText("");
        } else {
            dbHandler.addChat(chatMessage);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    public void imageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void handleVoiceQuery() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_PERMISSION_REQUEST);
        } else {
            assistant.startListening();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RECORD_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    assistant.startListening();
                } else {
                    Toast.makeText(getContext(), "Record permissions not granted", Toast.LENGTH_SHORT).show();
                }
        }
    }
}
