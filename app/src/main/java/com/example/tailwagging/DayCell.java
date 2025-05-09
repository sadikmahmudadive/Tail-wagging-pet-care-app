package com.example.tailwagging;

public class DayCell {
    public String dayText;
    public boolean isOtherMonth;
    public boolean isToday;
    public boolean isSelected;

    public DayCell(String dayText, boolean isOtherMonth, boolean isToday, boolean isSelected) {
        this.dayText = dayText;
        this.isOtherMonth = isOtherMonth;
        this.isToday = isToday;
        this.isSelected = isSelected;
    }
}