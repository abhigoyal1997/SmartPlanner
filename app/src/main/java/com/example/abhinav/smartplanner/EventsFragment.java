package com.example.abhinav.smartplanner;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends Fragment {


    private OnFragmentInteractionListener mListener;
    private FirestoreRecyclerAdapter<CalEvent, CalItemHolder> adapter;

    private RecyclerView eventsView;
    private ProgressBar progressBar;

    private DBHandler dbHandler;
    String uid;



    public EventsFragment() {
        // Required empty public constructor
    }

    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View r = inflater.inflate(R.layout.fragment_events, container, false);

        eventsView = r.findViewById(R.id.events_recycler_view);
        progressBar = r.findViewById(R.id.loading_progress);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        eventsView.setLayoutManager(linearLayoutManager);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user != null ? user.getUid() : "";

        dbHandler = DBHandler.getInstance();
        dbHandler.init(uid);

        Query query = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("events").whereEqualTo("recur", false)
                .orderBy("date"); //To be filled

        FirestoreRecyclerOptions<CalEvent> options = new FirestoreRecyclerOptions.Builder<CalEvent>()
                .setQuery(query, CalEvent.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<CalEvent, CalItemHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CalItemHolder viewHolder, int position, @NonNull CalEvent model) {
                viewHolder.object = model;
                viewHolder.title.setText(model.getName());
                Long millis = model.getFrom();
                String fm = String.format(Locale.US, "%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
                millis = model.getTo();
                String tm = String.format(Locale.US, "%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
                Date date = new Date(model.date);
                String[] dateParts = date.toString().split(" ");
                String date_s = dateParts[0] + " " + dateParts[1] + " " + dateParts[2] + " " + dateParts[5];
                viewHolder.timestamp.setText("On " + date_s + " From " + fm + " to " + tm);
            }

            @NonNull
            @Override
            public CalItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item, parent, false);
                return new CalItemHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                progressBar.setVisibility(View.INVISIBLE);
                eventsView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Toast.makeText(getContext(), "There was a problem while loading the events!", Toast.LENGTH_SHORT).show();
            }
        };

        eventsView.setAdapter(adapter);

        return r;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
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
}
