package com.example.abhinav.smartplanner;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity implements View.OnClickListener{
    Button addTaskDone;
    EditText e_title;
    Button btnDatePicker, btnTimePicker;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        addTaskDone = findViewById(R.id.addTaskDone);
        e_title = findViewById(R.id.title);

        btnDatePicker=findViewById(R.id.btn_date);
        btnTimePicker=findViewById(R.id.btn_time);
        txtDate=findViewById(R.id.in_date);
        txtTime=findViewById(R.id.in_time);

        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);

        addTaskDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = e_title.getText().toString();
                String date = txtDate.getText().toString();
                String time = txtTime.getText().toString();

                Long dateMilli = null, timeMilli;
                String[] timeParts = time.split(":");
                timeMilli = (Long.valueOf(timeParts[0]) * 60 + Long.valueOf(timeParts[1])) * 60 * 1000;
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                Date date_d = null;
                try {
                    date_d = dateFormat.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date_d != null) {
                    dateMilli = date_d.getTime();
                }

                Intent returnIntent = new Intent();
                returnIntent.putExtra("title", title);
                returnIntent.putExtra("date", dateMilli);
                returnIntent.putExtra("time", timeMilli);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnDatePicker) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            String dayOfMonthString, monthOfYearString;
                            if(String.valueOf(dayOfMonth).length() == 1){
                                dayOfMonthString = "0" + Integer.toString(dayOfMonth);
                            } else{
                                dayOfMonthString = Integer.toString(dayOfMonth);
                            }
                            if(String.valueOf(monthOfYear+1).length() == 1){
                                monthOfYearString = "0" + Integer.toString(monthOfYear+1);
                            } else{
                                monthOfYearString = Integer.toString(monthOfYear+1);
                            }
                            txtDate.setText(dayOfMonthString + "-" + (monthOfYearString) + "-" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        if (v == btnTimePicker) {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            txtTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    }
}
