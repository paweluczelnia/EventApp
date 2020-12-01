package com.example.eventapp;

import java.io.Serializable;

public class EventPlan implements Serializable {
    String Id;
    String EventId;
    String Name;
    String Description;
    String StartTime;
    String EndTime;
    String AddedDate;
    String UpdatedDate;

    public EventPlan() {}

    public EventPlan(String id, String eventId, String name, String description, String startTime,
                     String EndTime) {
        this.Id = id;
        this.EventId = eventId;
        this.Name = name;
        this.Description = description;
        this.StartTime = startTime;
        this.EndTime = EndTime;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getEventId() {
        return EventId;
    }

    public void setEventId(String eventId) {
        EventId = eventId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public String getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(String addedDate) {
        AddedDate = addedDate;
    }

    public String getUpdatedDate() {
        return UpdatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        UpdatedDate = updatedDate;
    }
}
