package com.example.abhinav.smartplanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.abhinav.smartplanner.Constants.DATA;
import static com.example.abhinav.smartplanner.Constants.STATUS;
import static com.example.abhinav.smartplanner.Constants.STATUS_OK;

public class TasksFragment extends Fragment {

    String uid;

    View rootView;
    Button addTaskButton;
    DBHandler dbHandler;

    private FirestoreRecyclerAdapter<ToDoTask, ToDoTaskViewHolder> adapter;
    private RecyclerView taskView;
    private ProgressBar progressBar;
    FirestoreRecyclerOptions<ToDoTask> options;

    private OnFragmentInteractionListener mListener;

    public TasksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static TasksFragment newInstance() {
        TasksFragment fragment = new TasksFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tasks, container, false);
        addTaskButton = rootView.findViewById(R.id.addTasksButton);
        dbHandler = DBHandler.getInstance();





        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user != null ? user.getUid() : "";
        taskView = rootView.findViewById(R.id.task_recycler_view);
        progressBar = rootView.findViewById(R.id.task_loading_progress);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        taskView.setLayoutManager(linearLayoutManager);
        Query query = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("tasks").orderBy("timestamp");

        options = new FirestoreRecyclerOptions.Builder<ToDoTask>()
                .setQuery(query, ToDoTask.class).build();

        adapter = new FirestoreRecyclerAdapter<ToDoTask, ToDoTaskViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ToDoTaskViewHolder viewHolder, int position, @NonNull ToDoTask model) {
                viewHolder.title.setText(model.getTitle());
                viewHolder.timestamp.setText(model.getDate() + " at " + model.getTime());
            }

            @NonNull
            @Override
            public ToDoTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_chat, parent, false);
                return new ToDoTaskViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                progressBar.setVisibility(View.INVISIBLE);
                taskView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Toast.makeText(getContext(), "There was a problem while loading the tasks!", Toast.LENGTH_SHORT).show();
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
                    taskView.scrollToPosition(positionStart);
                }
            }
        });

        taskView.setAdapter(adapter);







        return rootView;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddTaskActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String title = data.getStringExtra("title");
            String date = data.getStringExtra("date");
            String time = data.getStringExtra("time");
            ToDoTask toDoTask = new ToDoTask(title, date, time);
            dbHandler.addToDoTask(toDoTask, new OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) throws JSONException {
                    if (response.getInt(STATUS) == STATUS_OK) {

                    } else {
                        Toast.makeText(getContext(), response.getString(DATA), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
