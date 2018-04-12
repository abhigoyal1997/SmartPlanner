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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.abhinav.smartplanner.Constants.DATA;
import static com.example.abhinav.smartplanner.Constants.STATUS;
import static com.example.abhinav.smartplanner.Constants.STATUS_OK;

public class TasksFragment extends Fragment {

    String uid;

    View rootView;
    DBHandler dbHandler;

    private FirestoreRecyclerAdapter<ToDoTask, CalItemHolder> adapter;
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
        setHasOptionsMenu(true);

    }

    public static TasksFragment newInstance() {
        TasksFragment fragment = new TasksFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tasks, container, false);
        dbHandler = DBHandler.getInstance();





        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user != null ? user.getUid() : "";
        taskView = rootView.findViewById(R.id.task_recycler_view);
        progressBar = rootView.findViewById(R.id.task_loading_progress);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        taskView.setLayoutManager(linearLayoutManager);
        Query query = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("tasks").orderBy("date");

        options = new FirestoreRecyclerOptions.Builder<ToDoTask>()
                .setQuery(query, ToDoTask.class).build();

        adapter = new FirestoreRecyclerAdapter<ToDoTask, CalItemHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CalItemHolder viewHolder, int position, @NonNull ToDoTask model) {
                viewHolder.object = model;
                viewHolder.title.setText(model.getTitle());
                Long millis = model.getTime();
                String hm = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), Locale.US);
                Date date = new Date(model.getDate());
                String[] dateParts = date.toString().split(" ");
                String date_s = dateParts[0] + " " + dateParts[1] + " " + dateParts[2] + " " + dateParts[5];
                viewHolder.timestamp.setText(date_s + " at " + hm);
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
                taskView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                super.onError(e);
                Toast.makeText(getContext(), "There was a problem while loading the calItems!", Toast.LENGTH_SHORT).show();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_task, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.addTasksButton:
                Intent intent = new Intent(getActivity(), AddTaskActivity.class);
                startActivityForResult(intent, 1);
                break;
        }
        return true;
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
    public void onStart(){
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop(){
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String title = data.getStringExtra("title");
            Long date = data.getLongExtra("date", -1);
            Long time = data.getLongExtra("time",-1);
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
