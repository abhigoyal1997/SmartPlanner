package com.example.abhinav.smartplanner;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by abhi on 7/4/18.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder  {
    public TextView leftText;
    public TextView rightText;

    public ChatViewHolder(View itemView){
        super(itemView);
        leftText = itemView.findViewById(R.id.leftText);
        rightText = itemView.findViewById(R.id.rightText);
    }
}