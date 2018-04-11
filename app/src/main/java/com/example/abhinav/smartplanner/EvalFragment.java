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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EvalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EvalFragment extends Fragment {


    private OnFragmentInteractionListener mListener;
    private FirestoreRecyclerAdapter<CalEvent, CalEventHolder> adapter;

    private RecyclerView eventsView;
    private ProgressBar progressBar;

    private DBHandler dbHandler;


    public EvalFragment() {
        // Required empty public constructor
    }

    public static EvalFragment newInstance() {
        EvalFragment fragment = new EvalFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View r = inflater.inflate(R.layout.fragment_eval, container, false);

        eventsView = r.findViewById(R.id.events_recycler_view);
        progressBar = r.findViewById(R.id.loading_progress);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        eventsView.setLayoutManager(linearLayoutManager);


        dbHandler = DBHandler.getInstance();


        Query query = FirebaseFirestore.getInstance()
                .collection("events") //Change to correct database
                .whereEqualTo("recur", false)
                .orderBy("timestamp")   // Timestamp seems fine
                ;

        FirestoreRecyclerOptions<CalEvent> options = new FirestoreRecyclerOptions.Builder<CalEvent>()
                .setQuery(query, CalEvent.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<CalEvent, CalEventHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CalEventHolder viewHolder, int position, @NonNull CalEvent model) {
                viewHolder.nameColumn.setText(model.getName());
                viewHolder.toColumn.setText(model.getTo());
            }

            @NonNull
            @Override
            public CalEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_event, parent, false);
                return new CalEventHolder(view);
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
