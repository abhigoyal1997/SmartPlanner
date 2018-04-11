package com.example.abhinav.smartplanner;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by anmol on 11/04/18.
 */

public class CalEventHolder extends RecyclerView.ViewHolder{
    public TextView nameColumn;
    public TextView toColumn;

    public CalEventHolder(View itemView){
        super(itemView);
        nameColumn = itemView.findViewById(R.id.nameColumn);
        toColumn = itemView.findViewById(R.id.toColumn);
    }
}
