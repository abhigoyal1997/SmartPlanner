package com.example.abhinav.smartplanner;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by shraddheya on 11/4/18.
 */

class ToDoTaskViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public TextView timestamp;

    public ToDoTaskViewHolder(View itemView){
        super(itemView);
        title = itemView.findViewById(R.id.firstLine);
        timestamp = itemView.findViewById(R.id.secondLine);
    }
}
