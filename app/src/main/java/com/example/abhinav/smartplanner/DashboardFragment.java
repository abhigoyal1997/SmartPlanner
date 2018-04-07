package com.example.abhinav.smartplanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.TokenWatcher;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class DashboardFragment extends Fragment {

    private VirtualAssistant assistant;
    private final int RECORD_PERMISSION_REQUEST = 99;

    private OnFragmentInteractionListener mListener;
    private EditText queryEditText;
    private Button queryButton;

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View r = inflater.inflate(R.layout.fragment_dashboard, container, false);
        assistant = new VirtualAssistant();
        queryEditText = r.findViewById(R.id.query);
        queryButton = r.findViewById(R.id.query_button);
        queryEditText.setVisibility(View.INVISIBLE);
        queryButton.setVisibility(View.INVISIBLE);
        final InputMethodManager inputMethodManager;
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        FloatingActionButton fab = (FloatingActionButton) r.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryEditText.setVisibility(View.VISIBLE);
                queryButton.setVisibility(View.VISIBLE);
                queryEditText.requestFocus();
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(queryEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = queryEditText.getText().toString();
                if (!query.equals("")) {
                    assistant.handleQuery(queryEditText.getText().toString());
                    queryEditText.setText("");
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    }
                    queryEditText.setVisibility(View.INVISIBLE);
                    queryButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        return r;
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

    public boolean checkRecordPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_PERMISSION_REQUEST);
        } else {
            return true;
        }
        return false;
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
