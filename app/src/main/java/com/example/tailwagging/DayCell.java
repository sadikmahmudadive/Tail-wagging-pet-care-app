package com.example.tailwagging;

public class DayCell {
    public String dayText;
    public boolean isOtherMonth;
    public boolean isToday;
    public boolean isSelected;
    public boolean hasEvents;

    public DayCell(String dayText, boolean isOtherMonth, boolean isToday, boolean isSelected, boolean hasEvents) {
        this.dayText = dayText;
        this.isOtherMonth = isOtherMonth;
        this.isToday = isToday;
        this.isSelected = isSelected;
        this.hasEvents = hasEvents;
    }
}