package com.example.abhinav.smartplanner;

import android.content.Context;
import android.net.Uri;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.abhinav.smartplanner.Constants.DATA;
import static com.example.abhinav.smartplanner.Constants.STATUS;
import static com.example.abhinav.smartplanner.Constants.STATUS_OK;

public class CalendarFragment extends Fragment {
    private OnFragmentInteractionListener mListener = null;

    private MaterialCalendarView calendarView = null;

    DBHandler dbHandler;
    String uid;

    private RecyclerView.Adapter<ToDoTaskViewHolder> taskAdapter;
//    private RecyclerView.Adapter<CalEventHolder> eventAdapter;
    private RecyclerView taskView;
    private RecyclerView eventView;
    List<ToDoTask> tasks = new ArrayList<>();
    List<CalEvent> events = new ArrayList<>();

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendar_view);
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);
        selectDate(CalendarDay.today());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user != null ? user.getUid() : "";
        dbHandler = DBHandler.getInstance();

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                fetchCalList(date);
            }
        });

        taskView = view.findViewById(R.id.calendar_recycler_view);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        taskView.setLayoutManager(linearLayoutManager);

        taskAdapter = new RecyclerView.Adapter<ToDoTaskViewHolder>() {
            @Override
            public void onBindViewHolder(@NonNull ToDoTaskViewHolder viewHolder, int position) {
                ToDoTask model = tasks.get(position);
                viewHolder.title.setText(model.getTitle());
                Long millis = model.getTime();
                String hm = String.format(Locale.US, "%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
                Date date = new Date(model.getDate());
                String[] dateParts = date.toString().split(" ");
                String date_s = dateParts[0] + " " + dateParts[1] + " " + dateParts[2] + " " + dateParts[5];
                viewHolder.timestamp.setText(date_s + " at " + hm);
            }

            @NonNull
            @Override
            public ToDoTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.task_list_item, parent, false);
                return new ToDoTaskViewHolder(view);
            }

            @Override
            public int getItemCount() {
                return tasks.size();
            }
        };

        taskAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = taskAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    taskView.scrollToPosition(positionStart);
                }
            }
        });

        taskView.setAdapter(taskAdapter);
        fetchCalList(CalendarDay.today());

        return view;
    }

    private void fetchCalList(CalendarDay date) {
        dbHandler.getTasks(date.getDate().getTime(), date.getDate().getTime(), new OnResponseListener() {
            @Override
            public void onResponse(JSONObject response) throws JSONException {
                if (response.getInt(STATUS) == STATUS_OK) {
                    tasks = (List<ToDoTask>) response.get(DATA);
                    taskAdapter.notifyDataSetChanged();
                } else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unable to fetch today's tasks", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dbHandler.getEvents(date.getDate().getTime(), date.getDate().getTime(), new OnResponseListener() {
            @Override
            public void onResponse(JSONObject response) throws JSONException {
                if (response.getInt(STATUS) == STATUS_OK) {
                    events = (List<CalEvent>) response.get(DATA);
                    taskAdapter.notifyDataSetChanged();
                } else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unable to fetch today's events", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.calendar_action, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.cal_action_today:
                selectDate(CalendarDay.today());
                fetchCalList(CalendarDay.today());
                break;
        }
        return true;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

    private void selectDate(CalendarDay day) {
        calendarView.setSelectedDate(day);
        calendarView.setCurrentDate(day);
    }
}
