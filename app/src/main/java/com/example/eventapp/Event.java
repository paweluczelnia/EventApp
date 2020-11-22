package com.example.eventapp;

import java.io.Serializable;

public class Event implements Serializable {
    public String AuthorId, Coordinates, EventDate, EventTime, Name;
    public int Ticket;

    public Event() {

    }

    public Event(String authorId, String coordinates, String eventDate, String eventTime,
                 String name, int ticket) {
        AuthorId = authorId;
        Coordinates = coordinates;
        EventDate = eventDate;
        EventTime = eventTime;
        Name = name;
        Ticket = ticket;
    }
}
