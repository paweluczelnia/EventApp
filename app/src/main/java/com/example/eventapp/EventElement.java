package com.example.eventapp;

import java.io.Serializable;

public class EventElement implements Serializable {
    public String EventElementName, EventElementDesc, EventElementDate, EventElementStartTime, EventElementEndTime;

    public EventElement() {

    }

    public EventElement(String eventName, String descriptionElement, String dateElement, String startTime,
                 String endTime) {
        EventElementName = eventName;
        EventElementDesc = descriptionElement;
        EventElementDate = dateElement;
        EventElementStartTime = startTime;
        EventElementEndTime = endTime;
    }
}
