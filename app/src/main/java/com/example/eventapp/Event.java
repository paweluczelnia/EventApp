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

    public String getAuthorId() {
        return AuthorId;
    }

    public void setAuthorId(String authorId) {
        AuthorId = authorId;
    }

    public String getCoordinates() {
        return Coordinates;
    }

    public void setCoordinates(String coordinates) {
        Coordinates = coordinates;
    }

    public String getEventDate() {
        return EventDate;
    }

    public void setEventDate(String eventDate) {
        EventDate = eventDate;
    }

    public String getEventTime() {
        return EventTime;
    }

    public void setEventTime(String eventTime) {
        EventTime = eventTime;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getTicket() {
        return Ticket;
    }

    public void setTicket(int ticket) {
        Ticket = ticket;
    }
}
