package com.example.eventapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class EventPlanAdapter extends ArrayAdapter<EventPlan> {

    private ArrayList<EventPlan> dataSet;
    Context context;

    public EventPlanAdapter(ArrayList<EventPlan> data, Context context) {
        super(context, R.layout.event_plan_item, data);
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if (convertView == null) {
            convertView =  LayoutInflater.from(context).inflate(R.layout.event_plan_item, parent, false);
        }

        EventPlan ev = getItem(position);

        TextView startTime = convertView.findViewById(R.id.startTime);
        TextView endTime = convertView.findViewById(R.id.endTime);
        TextView namePlan = convertView.findViewById(R.id.namePlan);
        TextView planDescription = convertView.findViewById(R.id.planDescription);

        startTime.setText(ev.getStartTime());
        endTime.setText(ev.getEndTime());
        namePlan.setText(ev.getName());
        planDescription.setText(ev.getDescription());

        return convertView;
    }
}
