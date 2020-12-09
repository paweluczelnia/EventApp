package com.example.eventapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class EventsAdapter extends ArrayAdapter<Event> {
    private ArrayList<Event> dataSet;
    Context context;

    public EventsAdapter(ArrayList<Event> data, Context context) {
        super(context, R.layout.event_item, data);
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if (convertView == null) {
            convertView =  LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        }

        Event ev = getItem(position);

        TextView time = convertView.findViewById(R.id.evTime);
        TextView date = convertView.findViewById(R.id.evDate);
        TextView name = convertView.findViewById(R.id.evName);
        TextView location = convertView.findViewById(R.id.evLocation);

        time.setText(ev.getEventTime());
        date.setText(ev.getEventDate());
        name.setText(ev.getName());
        location.setText(ev.getLocationName());



        return convertView;
    }
}
