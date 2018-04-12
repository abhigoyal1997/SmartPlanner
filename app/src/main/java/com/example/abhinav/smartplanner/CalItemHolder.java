package com.example.abhinav.smartplanner;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shraddheya on 11/4/18.
 */

class CalItemHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
    public Object object;
    public TextView title;
    public TextView timestamp;

    public CalItemHolder(View itemView){
        super(itemView);
        title = itemView.findViewById(R.id.firstLine);
        timestamp = itemView.findViewById(R.id.secondLine);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View view) {
        Log.d("click", view.toString());
        new AlertDialog.Builder(App.get().homeActivity)
            //set message, title, and icon
            .setTitle("Delete")
            .setMessage("Do you want to Delete")
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    if (object instanceof CalEvent) {
                        DBHandler.getInstance().deleteEvent(((CalEvent) object).id, new OnResponseListener() {
                            @Override
                            public void onResponse(JSONObject response) throws JSONException {
                            }
                        });
                    } else {
                        DBHandler.getInstance().deleteTask(((ToDoTask) object).id, new OnResponseListener() {
                            @Override
                            public void onResponse(JSONObject response) throws JSONException {
                            }
                        });
                    }
                    dialog.dismiss();
                }

            }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            })
            .create().show();
        return true;
    }
}
