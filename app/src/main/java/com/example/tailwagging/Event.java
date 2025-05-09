package com.example.tailwagging;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Event implements Serializable {
    public int id;
    public String title;
    public String category;
    public String note;
    public LocalDate date;
    public LocalTime fromTime;
    public LocalTime toTime;

    public Event(int id, String title, String category, String note, LocalDate date, LocalTime fromTime, LocalTime toTime) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.note = note;
        this.date = date;
        this.fromTime = fromTime;
        this.toTime = toTime;
    }
}