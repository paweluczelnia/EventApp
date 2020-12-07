package com.example.eventapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class Event implements Serializable {
    public String Id, EventId, AuthorId, Coordinates, EventDate, EventTime, Name, LocationName;
    public int Ticket;

    public Event() {

    }

    public Event(String eventId, String authorId, String coordinates, String eventDate, String eventTime,
                 String name, int ticket) {
        AuthorId = authorId;
        Coordinates = coordinates;
        EventDate = eventDate;
        EventTime = eventTime;
        Name = name;
        Ticket = ticket;
    }

    public void CalculateLocation(Context context, String coordinates)
    {
        Address address = getFullAddress(context, coordinates);
        String addressLine = address.getAddressLine(0);
        if (addressLine != null) {
            String[] separatedAddress = addressLine.split(",");
            addressLine = addressLine.replace(",", System.getProperty("line.separator"));
            if (addressLine != null) {
                this.LocationName = separatedAddress[0] + System.getProperty("line.separator")
                        + separatedAddress[1];
            }
        }
    }

    public void LocationWithoutPostalCode(Context context, String coordinates)
    {
        Address address = getFullAddress(context, coordinates);
        String addressLine = address.getAddressLine(0);
        if (addressLine != null) {
            String[] separatedAddress = addressLine.split(",");
            addressLine = addressLine.replace(",", System.getProperty("line.separator"));
            if (addressLine != null) {
                this.LocationName = separatedAddress[0] + ", " + address.getLocality();
            }
        }
    }

    private Address getFullAddress(Context context, String coordinates)
    {
        String[] separatedCoords = coordinates.split(";");
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(Double.parseDouble(separatedCoords[0]),
                    Double.parseDouble(separatedCoords[1]), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null)
        {
            Log.d("TAG", "ADDRESS: " + addresses.get(0));
            return addresses.get(0);
        }

        return null;
    }

    //#region setters/getters
    public String getLocationName() { return LocationName; }

    public void setLocationName(String locationName) { LocationName = locationName; }

    public String getId() { return Id; }

    public void setId(String id) { Id = id; }

    public String getEventId() {
        return EventId;
    }

    public void setEventId(String eventId) {
        EventId = eventId;
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
    //#endregion
}
