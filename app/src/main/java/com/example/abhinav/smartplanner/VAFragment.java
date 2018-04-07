package com.example.abhinav.smartplanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class VAFragment extends Fragment {

    private VirtualAssistant assistant;
    private final int RECORD_PERMISSION_REQUEST = 99;

    private OnFragmentInteractionListener mListener;

    private RecyclerView chatView;
    private EditText newChatMsg;
    private RelativeLayout chatSendView;
    private DatabaseReference dbRef;
    private FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder> chatAdapter;

    private boolean flagFab = true;

    public VAFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        final View r = inflater.inflate(R.layout.fragment_va, container, false);
        assistant = new VirtualAssistant(this);

        chatView = r.findViewById(R.id.chat_recycler_view);
        newChatMsg = r.findViewById(R.id.chat_msg);
        chatSendView = r.findViewById(R.id.chat_send);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        chatView.setLayoutManager(linearLayoutManager);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        chatSendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = newChatMsg.getText().toString().trim();
                if (!message.equals("")) {
                    ChatMessage chatMessage = new ChatMessage(message, "user");
                    dbRef.child("chat").push().setValue(chatMessage);
                    assistant.handleQuery(message);
                    newChatMsg.setText("");
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
                Bitmap img = BitmapFactory.decodeResource(getResources(),R.drawable.ic_send_white);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mic_white);


                if (s.toString().trim().length()!=0 && flagFab){
                    imageViewAnimatedChange(getContext(),fab_img,img);
                    flagFab=false;

                }
                else if (s.toString().trim().length()==0){
                    imageViewAnimatedChange(getContext(),fab_img,img1);
                    flagFab=true;

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        chatAdapter = new FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder>(ChatMessage.class, R.layout.chat_list, ChatViewHolder.class, dbRef.child("chat")) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, ChatMessage model, int position) {
                if (model.getMsgUser().equals("user")) {
                    viewHolder.rightText.setText(model.getMsgText());
                    viewHolder.rightText.setVisibility(View.VISIBLE);
                    viewHolder.leftText.setVisibility(View.GONE);
                } else {
                    viewHolder.leftText.setText(model.getMsgText());
                    viewHolder.leftText.setVisibility(View.VISIBLE);
                    viewHolder.rightText.setVisibility(View.GONE);
                }
            }
        };

        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = chatAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    chatView.scrollToPosition(positionStart);
                }
            }
        });

        chatView.setAdapter(chatAdapter);

        return r;
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


    public void onAIQueryResult(String reply) {
        Log.d("result", reply);
        if (!reply.equals("")) {
            ChatMessage chatMessage = new ChatMessage(reply, "bot");
            dbRef.child("chat").push().setValue(chatMessage);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
