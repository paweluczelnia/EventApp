package com.example.eventapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Event> events;

    public MyAdapter(Context c, ArrayList<Event> e) {

        context = c;
        events = e;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.name.setText(events.get(position).getName());
        holder.date.setText(events.get(position).getEventDate());
        holder.location.setText(events.get(position).getCoordinates());

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount(){
        return events.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name, date, location;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name=(TextView)itemView.findViewById(R.id.nametext);
            date=(TextView)itemView.findViewById(R.id.datetext);
            location=(TextView)itemView.findViewById(R.id.navGPS);
        }
    }
}
